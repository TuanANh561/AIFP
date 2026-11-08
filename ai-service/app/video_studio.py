"""Video Studio: script -> scenes -> short clips -> one smooth video (<= 5 minutes).

Design notes
- No generative model can output a 5 minute video in one call; the studio plans N short clips
  (default 5s), chains them (last frame of clip k is the first frame of clip k+1 so the character
  and the scene stay continuous) and joins them with cross-fades using FFmpeg.
- Clip generation sits behind `ClipProvider`, so a GPU model (Wan 2.2 via ComfyUI) or a paid API can be
  plugged in without touching planning/assembly. `KenBurnsProvider` is a real, GPU-free provider that
  animates the still image (slow pan/zoom); it is NOT AI motion.
- The assembler never draws a watermark, logo or "AI" text on the picture. Providers that add their
  own visible watermark (some free tiers of paid APIs) are the caller's responsibility.
- Cost gate: `run_video_job` refuses to start unless `approved=True` (Level 3 HITL in the platform).
"""
from __future__ import annotations

import math
import re
import shutil
import subprocess
from dataclasses import dataclass, field
from pathlib import Path
from typing import Protocol

MAX_TOTAL_SECONDS = 300.0
DEFAULT_CLIP_SECONDS = 5.0
DEFAULT_CROSSFADE_SECONDS = 0.5
MAX_SCENES = 80
OUT_WIDTH, OUT_HEIGHT, OUT_FPS = 1280, 720, 24


@dataclass(frozen=True)
class Scene:
    index: int
    prompt: str
    narration: str
    duration: float


@dataclass(frozen=True)
class VideoPlan:
    scenes: list[Scene]
    clip_seconds: float
    crossfade_seconds: float
    character_lock: str = ""

    @property
    def total_seconds(self) -> float:
        n = len(self.scenes)
        return n * self.clip_seconds - max(0, n - 1) * self.crossfade_seconds


class ClipProvider(Protocol):
    def generate(self, scene: Scene, start_image: Path, out_path: Path) -> Path:
        """Render one clip of `scene.duration` seconds beginning at `start_image`."""


def find_ffmpeg() -> str:
    exe = shutil.which("ffmpeg")
    if exe:
        return exe
    try:  # optional bundled binary: pip install imageio-ffmpeg
        import imageio_ffmpeg  # type: ignore

        return imageio_ffmpeg.get_ffmpeg_exe()
    except Exception as exc:  # pragma: no cover - depends on host
        raise RuntimeError("FFmpeg not found. Install ffmpeg or `pip install imageio-ffmpeg`.") from exc


def _run(cmd: list[str]) -> None:
    proc = subprocess.run(cmd, capture_output=True, text=True)
    if proc.returncode != 0:
        raise RuntimeError(f"ffmpeg failed: {proc.stderr[-600:]}")


def split_sentences(script: str) -> list[str]:
    parts = re.split(r"(?<=[.!?…])\s+|\n+", script.strip())
    return [p.strip() for p in parts if p.strip()]


def plan_scenes(
    script: str,
    target_seconds: float,
    character_lock: str = "",
    clip_seconds: float = DEFAULT_CLIP_SECONDS,
    crossfade_seconds: float = DEFAULT_CROSSFADE_SECONDS,
) -> VideoPlan:
    """Split a script into evenly timed scenes. Total length is capped at 5 minutes."""
    if not script.strip():
        raise ValueError("Script is empty")
    if clip_seconds <= crossfade_seconds * 2:
        raise ValueError("clip_seconds must be longer than twice the crossfade")
    target = min(float(target_seconds), MAX_TOTAL_SECONDS)
    if target <= clip_seconds:
        n = 1
    else:
        n = math.ceil((target - crossfade_seconds) / (clip_seconds - crossfade_seconds))
    while n > 1 and n * clip_seconds - (n - 1) * crossfade_seconds > MAX_TOTAL_SECONDS:
        n -= 1
    n = min(n, MAX_SCENES)

    sentences = split_sentences(script)
    lock = character_lock.strip()
    scenes: list[Scene] = []
    for i in range(n):
        sentence = sentences[min(len(sentences) - 1, i * len(sentences) // n)]
        prompt = f"{lock}. {sentence}" if lock else sentence
        scenes.append(Scene(index=i, prompt=prompt, narration=sentence, duration=clip_seconds))
    return VideoPlan(scenes=scenes, clip_seconds=clip_seconds, crossfade_seconds=crossfade_seconds, character_lock=lock)


def extract_last_frame(video: Path, out_png: Path) -> Path:
    _run([find_ffmpeg(), "-y", "-sseof", "-0.15", "-i", str(video), "-frames:v", "1", "-update", "1", str(out_png)])
    if not out_png.exists():
        raise RuntimeError("Could not extract last frame")
    return out_png


def build_xfade_filter(n: int, clip_seconds: float, crossfade: float) -> str:
    """Filter graph that normalises every clip then chains cross-fades. n >= 1."""
    norm = [
        f"[{i}:v]scale={OUT_WIDTH}:{OUT_HEIGHT}:force_original_aspect_ratio=increase,"
        f"crop={OUT_WIDTH}:{OUT_HEIGHT},setsar=1,fps={OUT_FPS},format=yuv420p[n{i}]"
        for i in range(n)
    ]
    if n == 1:
        return ";".join(norm) + ";[n0]null[vout]"
    chain = []
    prev = "n0"
    for k in range(1, n):
        offset = round(k * (clip_seconds - crossfade), 3)
        label = "vout" if k == n - 1 else f"x{k}"
        chain.append(f"[{prev}][n{k}]xfade=transition=fade:duration={crossfade}:offset={offset}[{label}]")
        prev = label
    return ";".join(norm + chain)


def assemble(clips: list[Path], out_path: Path, clip_seconds: float, crossfade: float) -> Path:
    if not clips:
        raise ValueError("No clips to assemble")
    cmd = [find_ffmpeg(), "-y"]
    for c in clips:
        cmd += ["-i", str(c)]
    cmd += [
        "-filter_complex", build_xfade_filter(len(clips), clip_seconds, crossfade),
        "-map", "[vout]", "-c:v", "libx264", "-preset", "medium", "-crf", "20",
        "-pix_fmt", "yuv420p", "-movflags", "+faststart", "-an", str(out_path),
    ]
    _run(cmd)
    return out_path


class KenBurnsProvider:
    """GPU-free provider: slow zoom over the start image. Real output, but not AI-generated motion."""

    def generate(self, scene: Scene, start_image: Path, out_path: Path) -> Path:
        frames = int(scene.duration * OUT_FPS)
        zoom = (
            f"scale=2560:1440:force_original_aspect_ratio=increase,crop=2560:1440,"
            f"zoompan=z='min(zoom+0.0009,1.15)':d={frames}:s={OUT_WIDTH}x{OUT_HEIGHT}:fps={OUT_FPS},format=yuv420p"
        )
        _run([
            find_ffmpeg(), "-y", "-loop", "1", "-i", str(start_image), "-vf", zoom,
            "-t", str(scene.duration), "-c:v", "libx264", "-pix_fmt", "yuv420p", str(out_path),
        ])
        return out_path


@dataclass
class VideoJobResult:
    video: Path
    clips: list[Path] = field(default_factory=list)
    seconds: float = 0.0


def run_video_job(
    plan: VideoPlan,
    provider: ClipProvider,
    character_image: Path,
    workdir: Path,
    approved: bool,
    chain_frames: bool = True,
) -> VideoJobResult:
    """Render all scenes and join them. Refuses to run without explicit approval (cost gate)."""
    if not approved:
        raise PermissionError("Video generation needs explicit user approval (WAITING_FOR_APPROVAL)")
    if len(plan.scenes) > MAX_SCENES or plan.total_seconds > MAX_TOTAL_SECONDS + 1e-6:
        raise ValueError("Plan exceeds the 5 minute / scene limit")
    workdir.mkdir(parents=True, exist_ok=True)

    start = character_image
    clips: list[Path] = []
    for scene in plan.scenes:
        clip = provider.generate(scene, start, workdir / f"clip_{scene.index:03d}.mp4")
        clips.append(clip)
        if chain_frames and scene.index < len(plan.scenes) - 1:
            start = extract_last_frame(clip, workdir / f"last_{scene.index:03d}.png")

    out = assemble(clips, workdir / "final.mp4", plan.clip_seconds, plan.crossfade_seconds)
    return VideoJobResult(video=out, clips=clips, seconds=plan.total_seconds)


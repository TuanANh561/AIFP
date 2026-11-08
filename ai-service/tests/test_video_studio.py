from __future__ import annotations

import json
import subprocess
from pathlib import Path

import pytest

from app import video_studio as vs


def test_plan_caps_at_five_minutes_and_counts_scenes():
    plan = vs.plan_scenes("Một. Hai. Ba.", target_seconds=9999, character_lock="Cô gái tóc đỏ")
    assert plan.total_seconds <= vs.MAX_TOTAL_SECONDS
    assert plan.total_seconds > vs.MAX_TOTAL_SECONDS - vs.DEFAULT_CLIP_SECONDS
    assert all(s.prompt.startswith("Cô gái tóc đỏ") for s in plan.scenes)
    assert len(vs.plan_scenes("x", 4).scenes) == 1


def test_plan_rejects_empty_script():
    with pytest.raises(ValueError):
        vs.plan_scenes("   ", 30)


def test_xfade_offsets_are_cumulative():
    graph = vs.build_xfade_filter(3, 5.0, 0.5)
    assert "offset=4.5" in graph and "offset=9.0" in graph and "[vout]" in graph


def test_job_requires_approval(tmp_path: Path):
    plan = vs.plan_scenes("a. b.", 10)
    with pytest.raises(PermissionError):
        vs.run_video_job(plan, vs.KenBurnsProvider(), tmp_path / "x.png", tmp_path, approved=False)


def _ffmpeg_ok() -> bool:
    try:
        vs.find_ffmpeg()
        return True
    except RuntimeError:
        return False


@pytest.mark.skipif(not _ffmpeg_ok(), reason="ffmpeg not available")
def test_end_to_end_no_gpu_pipeline_duration_and_no_audio(tmp_path: Path):
    ff = vs.find_ffmpeg()
    img = tmp_path / "character.png"
    subprocess.run([ff, "-y", "-f", "lavfi", "-i", "color=c=0x336699:s=640x360", "-frames:v", "1", str(img)], check=True, capture_output=True)
    plan = vs.plan_scenes("Cảnh một. Cảnh hai. Cảnh ba.", target_seconds=12, clip_seconds=5.0, crossfade_seconds=0.5)
    assert len(plan.scenes) == 3
    result = vs.run_video_job(plan, vs.KenBurnsProvider(), img, tmp_path / "job", approved=True)
    assert result.video.exists() and result.video.stat().st_size > 1000
    probe = subprocess.run([ff, "-i", str(result.video)], capture_output=True, text=True).stderr
    assert "1280x720" in probe and "Duration: 00:00:14" in probe


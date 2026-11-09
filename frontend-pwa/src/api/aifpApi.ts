export interface AuthSession {
  userId: string;
  email: string;
  displayName: string;
  accessToken: string;
  dailyTokenQuota: number;
  dailyTokensUsed: number;
}

const ENV_BASE_URL: string =
  typeof import.meta !== 'undefined' && (import.meta as unknown as { env?: Record<string, string> }).env?.VITE_API_BASE_URL
    ? String((import.meta as unknown as { env: Record<string, string> }).env.VITE_API_BASE_URL).replace(/\/+$/, '')
    : '';

class AifpApiClient {
  private accessToken: string | null = null;
  private apiBaseUrl: string = ENV_BASE_URL;

  public getBaseUrl(): string {
    return this.apiBaseUrl;
  }

  public setBaseUrl(url: string) {
    this.apiBaseUrl = url.trim().replace(/\/+$/, '');
  }

  private resolveUrl(path: string): string {
    return `${this.apiBaseUrl}${path}`;
  }

  public setAccessToken(token: string | null) {
    this.accessToken = token;
  }

  private headers(): HeadersInit {
    const h: Record<string, string> = { 'Content-Type': 'application/json' };
    if (this.accessToken) {
      h['Authorization'] = `Bearer ${this.accessToken}`;
    }
    return h;
  }

  public async checkBackendHealth(): Promise<boolean> {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/health'), {
        method: 'GET',
        headers: this.headers(),
      });
      return resp.ok;
    } catch {
      return false;
    }
  }

  public async login(email: string, password: string): Promise<AuthSession | null> {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/auth/login'), {
        method: 'POST',
        headers: this.headers(),
        body: JSON.stringify({ email, password }),
      });
      if (!resp.ok) return null;
      const json = await resp.json();
      if (json.success && json.data) {
        this.accessToken = json.data.accessToken;
        return json.data as AuthSession;
      }
    } catch {
      // Offline / standalone preview fallback
    }
    return null;
  }

  public async loginWithGoogle(idToken: string): Promise<AuthSession | null> {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/auth/google'), {
        method: 'POST',
        headers: this.headers(),
        body: JSON.stringify({ idToken }),
      });
      if (!resp.ok) return null;
      const json = await resp.json();
      if (json.success && json.data) {
        this.accessToken = json.data.accessToken;
        return json.data as AuthSession;
      }
    } catch {
      // Offline fallback
    }
    return null;
  }

  public async proposeCalendarEvent(
    title: string,
    startTime: string,
    endTime: string,
    location: string
  ): Promise<boolean> {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/calendar/events'), {
        method: 'POST',
        headers: this.headers(),
        body: JSON.stringify({ title, startTime, endTime, location }),
      });
      return resp.ok;
    } catch {
      return false;
    }
  }

  public async createWritingDraft(
    title: string,
    docType: string,
    tone: string,
    promptBrief: string
  ): Promise<boolean> {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/writing/draft'), {
        method: 'POST',
        headers: this.headers(),
        body: JSON.stringify({ title, docType, tone, promptBrief }),
      });
      return resp.ok;
    } catch {
      return false;
    }
  }

  public async updatePersonaProfile(personaType: string): Promise<boolean> {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/users/me/profile'), {
        method: 'PATCH',
        headers: this.headers(),
        body: JSON.stringify({ personaType }),
      });
      return resp.ok;
    } catch {
      return false;
    }
  }

  public async compareProducts(query: string) {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/shopping/compare'), {
        method: 'POST',
        headers: this.headers(),
        body: JSON.stringify({ query }),
      });
      if (!resp.ok) return null;
      const json = await resp.json();
      return json.data;
    } catch {
      return null;
    }
  }

  public async trackProduct(title: string, targetPrice: number, currentPrice: number, storeUrl: string, storeName: string): Promise<boolean> {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/shopping/items'), {
        method: 'POST',
        headers: this.headers(),
        body: JSON.stringify({ title, targetPrice, currentPrice, storeUrl, storeName }),
      });
      return resp.ok;
    } catch {
      return false;
    }
  }

  public async triggerEmergencyKillSwitch(): Promise<boolean> {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/security/emergency-stop'), {
        method: 'POST',
        headers: this.headers(),
      });
      return resp.ok;
    } catch {
      return false;
    }
  }

  public async getAiProviderPool() {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/ai-providers/pool'), {
        headers: this.headers(),
      });
      if (!resp.ok) return null;
      const json = await resp.json();
      return json.data;
    } catch {
      return null;
    }
  }

  public async registerAiKey(provider: string, apiKey: string): Promise<boolean> {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/ai-providers/keys'), {
        method: 'POST',
        headers: this.headers(),
        body: JSON.stringify({ provider, apiKey }),
      });
      return resp.ok;
    } catch {
      return false;
    }
  }

  public async getMyAiKeys() {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/ai-providers/my-keys'), {
        headers: this.headers(),
      });
      if (!resp.ok) return [];
      const json = await resp.json();
      return json.data || [];
    } catch {
      return [];
    }
  }

  public async listVisionRecords() {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/vision/records'), {
        headers: this.headers(),
      });
      if (!resp.ok) return [];
      const json = await resp.json();
      return json.data || [];
    } catch {
      return [];
    }
  }

  public async analyzeVision(imageName: string, analysisMode: string, imageDataBase64?: string, prompt?: string) {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/vision/analyze'), {
        method: 'POST',
        headers: this.headers(),
        body: JSON.stringify({ imageName, analysisMode, imageDataBase64, prompt }),
      });
      if (!resp.ok) return null;
      const json = await resp.json();
      return json.data;
    } catch {
      return null;
    }
  }

  public async deleteVisionRecord(id: string): Promise<boolean> {
    try {
      const resp = await fetch(this.resolveUrl(`/api/v1/vision/records/${id}`), {
        method: 'DELETE',
        headers: this.headers(),
      });
      return resp.ok;
    } catch {
      return false;
    }
  }

  public async synthesizeTryon(garmentTitle: string, garmentCategory: string, userPhotoBase64?: string) {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/tryon/synthesize'), {
        method: 'POST',
        headers: this.headers(),
        body: JSON.stringify({ garmentTitle, garmentCategory, userPhotoBase64 }),
      });
      if (!resp.ok) return null;
      const json = await resp.json();
      return json.data;
    } catch {
      return null;
    }
  }

  public async listTryonSessions() {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/tryon/sessions'), {
        headers: this.headers(),
      });
      if (!resp.ok) return [];
      const json = await resp.json();
      return json.data || [];
    } catch {
      return [];
    }
  }

  public async pairPcDevice(deviceName: string, osType: string, sandboxFolderPath: string) {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/pc-companion/pair'), {
        method: 'POST',
        headers: this.headers(),
        body: JSON.stringify({ deviceName, osType, sandboxFolderPath }),
      });
      if (!resp.ok) return null;
      const json = await resp.json();
      return json.data;
    } catch {
      return null;
    }
  }

  public async listPcDevices() {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/pc-companion/devices'), {
        headers: this.headers(),
      });
      if (!resp.ok) return [];
      const json = await resp.json();
      return json.data || [];
    } catch {
      return [];
    }
  }

  public async createConversation(title: string) {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/chat/conversations'), {
        method: 'POST',
        headers: this.headers(),
        body: JSON.stringify({ title }),
      });
      if (!resp.ok) return null;
      const json = await resp.json();
      return json.data;
    } catch {
      return null;
    }
  }

  public async sendMessage(conversationId: string, content: string) {
    try {
      const resp = await fetch(this.resolveUrl(`/api/v1/chat/conversations/${conversationId}/messages`), {
        method: 'POST',
        headers: this.headers(),
        body: JSON.stringify({ content }),
      });
      if (!resp.ok) return null;
      const json = await resp.json();
      return json.data;
    } catch {
      return null;
    }
  }

  public async runTaskNow(taskId: string) {
    try {
      const resp = await fetch(this.resolveUrl(`/api/v1/tasks/${taskId}/run-now`), {
        method: 'POST',
        headers: this.headers(),
      });
      if (!resp.ok) return null;
      const json = await resp.json();
      return json.data;
    } catch {
      return null;
    }
  }

  public async sendChannelDigest(
    recipientEmail: string,
    channelId: string,
    channelTitle?: string,
    keywords?: string[],
    scheduledTime?: string
  ): Promise<{ success: boolean; message: string }> {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/notifications/send-channel-digest'), {
        method: 'POST',
        headers: this.headers(),
        body: JSON.stringify({
          recipientEmail,
          channelId,
          channelTitle,
          keywords,
          scheduledTime,
        }),
      });
      if (resp.ok) {
        const json = await resp.json();
        return {
          success: json.data?.delivered ?? true,
          message: json.data?.message ?? 'Đã gửi bản tin thành công!',
        };
      }
      return { success: false, message: 'Lỗi phản hồi từ máy chủ khi gửi bản tin.' };
    } catch {
      return { success: false, message: 'Không thể kết nối tới máy chủ backend.' };
    }
  }

  public async getLiveNews(channel?: string): Promise<LiveArticle[]> {
    try {
      const q = channel && channel !== 'all' ? `?channel=${encodeURIComponent(channel)}` : '';
      const resp = await fetch(this.resolveUrl(`/api/v1/news${q}`), {
        headers: this.headers(),
      });
      if (!resp.ok) return [];
      const json = await resp.json();
      return json.data || [];
    } catch {
      return [];
    }
  }

  public async syncLiveNews(): Promise<{ newAddedCount: number; totalInDatabase: number; syncedAt: string; status: string } | null> {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/news/sync-live'), {
        method: 'POST',
        headers: this.headers(),
      });
      if (!resp.ok) return null;
      const json = await resp.json();
      return json.data || null;
    } catch {
      return null;
    }
  }

  public async planVideo(params: {
    script: string;
    targetSeconds?: number;
    style?: string;
    voiceActor?: string;
    weatherEffect?: string;
    characterLock?: string;
  }): Promise<VideoStoryboard | null> {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/video/plan'), {
        method: 'POST',
        headers: this.headers(),
        body: JSON.stringify(params),
      });
      if (!resp.ok) return null;
      const json = await resp.json();
      return json.data || null;
    } catch {
      return null;
    }
  }

  public async createVideo(params: {
    title: string;
    script: string;
    targetSeconds?: number;
    style?: string;
    voiceActor?: string;
    weatherEffect?: string;
    characterLock?: string;
    approved: boolean;
  }): Promise<VideoCreationResult | null> {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/video/create'), {
        method: 'POST',
        headers: this.headers(),
        body: JSON.stringify(params),
      });
      if (!resp.ok) return null;
      const json = await resp.json();
      return json.data || null;
    } catch {
      return null;
    }
  }

  public async sendVideoEmail(params: {
    title: string;
    durationSeconds: number;
    sceneCount: number;
    style: string;
    voiceActor: string;
    weatherEffect: string;
    downloadUrl: string;
    customEmail?: string;
  }): Promise<{ success: boolean; message: string }> {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/video/send-email'), {
        method: 'POST',
        headers: this.headers(),
        body: JSON.stringify(params),
      });
      if (!resp.ok) return { success: false, message: 'Lỗi phản hồi từ máy chủ.' };
      const json = await resp.json();
      return { success: true, message: json.data || 'Đã gửi thông báo video qua email!' };
    } catch {
      return { success: false, message: 'Không thể kết nối tới máy chủ backend.' };
    }
  }

  public async editVideo(params: {
    videoUrl: string;
    trimStart?: number;
    trimEnd?: number;
    aspectRatio?: string;
    playbackSpeed?: number;
    captionsEnabled?: boolean;
    colorFilter?: string;
    musicVolumePercent?: number;
  }): Promise<{ status: string; editedVideoUrl: string; finalDuration: number; aspectRatio: string; speed: number; message: string } | null> {
    try {
      const resp = await fetch(this.resolveUrl('/api/v1/video/edit'), {
        method: 'POST',
        headers: this.headers(),
        body: JSON.stringify(params),
      });
      if (!resp.ok) return null;
      const json = await resp.json();
      return json.data || null;
    } catch {
      return null;
    }
  }
}

export interface VideoSceneItem {
  index: number;
  sceneNumber: number;
  narration: string;
  prompt: string;
  duration: number;
  cameraMotion: string;
  weatherOverlay: string;
  audioCue: string;
}

export interface VideoStoryboard {
  totalSeconds: number;
  sceneCount: number;
  style: string;
  voiceActor: string;
  voiceDescription: string;
  weatherEffect: string;
  weatherDescription: string;
  scenes: VideoSceneItem[];
}

export interface VideoCreationResult {
  status: string;
  title: string;
  totalSeconds: number;
  sceneCount: number;
  style: string;
  voiceActor: string;
  weatherEffect: string;
  hasWatermark: boolean;
  videoUrl: string;
  storyboard: VideoStoryboard;
  message: string;
}

export interface LiveArticle {
  id: string;
  channelId: string;
  source: string;
  sourceType: 'vn' | 'tech' | 'shop' | 'fb';
  category: string;
  title: string;
  summary: string;
  imageUrl: string;
  url: string;
  publishedAt: string;
  fetchedAt: string;
}

export const aifpApi = new AifpApiClient();
export default aifpApi;

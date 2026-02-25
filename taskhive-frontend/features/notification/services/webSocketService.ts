import { Client } from '@stomp/stompjs';
import type { WebSocketNotificationPayload } from '../types/notification.types';

class WebSocketService {
    private client: Client | null = null;
    private reconnectAttempts = 0;
    private readonly maxReconnectAttempts = 5;
    private reconnectTimeout: ReturnType<typeof setTimeout> | null = null;

    connect(
        userId: string,
        onNotification: (payload: WebSocketNotificationPayload) => void
    ): void {
        // Guard: do nothing if already connected or connecting
        if (this.client?.connected || this.client?.active) return;

        this.client = new Client({
            // Dynamic require forces CJS resolution at runtime — fixes Next.js Turbopack ESM incompatibility
            // eslint-disable-next-line @typescript-eslint/no-require-imports
            webSocketFactory: () => {
                const SockJS = require('sockjs-client');
                return new SockJS(`${process.env.NEXT_PUBLIC_WS_URL}/ws`);
            },

            onConnect: () => {
                this.reconnectAttempts = 0;
                console.log(`[WS] Connected — subscribing to /topic/notifications/${userId}`);

                // Subscribe to personal notification topic
                this.client!.subscribe(
                    `/topic/notifications/${userId}`,
                    (message) => {
                        try {
                            const payload: WebSocketNotificationPayload = JSON.parse(message.body);
                            onNotification(payload);
                        } catch (err) {
                            console.error('[WS] Failed to parse notification payload:', err);
                        }
                    }
                );
            },

            onStompError: (frame) => {
                console.error('[WS] STOMP error:', frame.headers['message']);
                this.scheduleReconnect(userId, onNotification);
            },

            onWebSocketError: (event) => {
                console.error('[WS] WebSocket connection error:', event);
                this.scheduleReconnect(userId, onNotification);
            },

            onDisconnect: () => {
                console.log('[WS] Disconnected');
            },

            // Let the library handle heartbeats
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        this.client.activate();
    }

    private scheduleReconnect(
        userId: string,
        onNotification: (payload: WebSocketNotificationPayload) => void
    ): void {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.warn('[WS] Max reconnect attempts reached. Giving up.');
            return;
        }
        this.reconnectAttempts++;
        const delay = Math.min(1000 * 2 ** this.reconnectAttempts, 30_000); // exponential backoff, max 30s
        console.log(
            `[WS] Reconnect attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts} in ${delay / 1000}s...`
        );
        this.reconnectTimeout = setTimeout(() => {
            this.connect(userId, onNotification);
        }, delay);
    }

    disconnect(): void {
        if (this.reconnectTimeout) {
            clearTimeout(this.reconnectTimeout);
            this.reconnectTimeout = null;
        }
        if (this.client) {
            this.client.deactivate();
            this.client = null;
        }
        this.reconnectAttempts = 0;
    }

    isConnected(): boolean {
        return this.client?.connected ?? false;
    }
}

// Singleton — do NOT instantiate this inside components
export const webSocketService = new WebSocketService();

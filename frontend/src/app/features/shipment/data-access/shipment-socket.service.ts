import { Injectable } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { Observable, Subject } from 'rxjs';
import { ShipmentStatusChangedEvent } from './shipment-status-changed-event.model';

@Injectable({ providedIn: 'root' })
export class ShipmentSocketService {
  private client: Client;
  private subjects = new Map<string, Subject<ShipmentStatusChangedEvent>>();
  private stompSubscriptions = new Map<string, StompSubscription>();

  constructor() {
    this.client = new Client({
      brokerURL: this.resolveSocketUrl(),
      reconnectDelay: 5000,
    });

    this.client.onConnect = () => {
      for (const trackingCode of this.subjects.keys()) {
        this.subscribeToTopic(trackingCode);
      }
    }

    this.client.activate();
  }

  private resolveSocketUrl(): string {
    const protocol = window.location.protocol === 'https' ? 'wss' : 'ws';
    return `${protocol}://${window.location.host}/ws`;
  }

  private subscribeToTopic(trackingCode: string): void {
    if (this.stompSubscriptions.has(trackingCode)) {
      return;
    }
    const subscription = this.client.subscribe(`/topic/shipments/${trackingCode}`,
      (message: IMessage) => {
        this.subjects.get(trackingCode)?.next(JSON.parse(message.body));
      });
    this.stompSubscriptions.set(trackingCode, subscription);
  }

  watch(trackingCode: string): Observable<ShipmentStatusChangedEvent> {
    let subject = this.subjects.get(trackingCode);

    if (!subject) {
      subject = new Subject<ShipmentStatusChangedEvent>();
      this.subjects.set(trackingCode, subject);

      if (this.client.connected) {
        this.subscribeToTopic(trackingCode);
      }
    }

    return subject.asObservable();
  }
}

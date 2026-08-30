import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { Observable, Subject } from 'rxjs';
import { LowStockEvent } from './inventory.model';

@Injectable({ providedIn: 'root' })
export class InventorySocketService {
  private client: Client;
  private subject = new Subject<LowStockEvent>()

  constructor() {
    this.client = new Client({
      brokerURL: this.resolveSocketUrl(),
      reconnectDelay: 5000,
    });

    this.client.onConnect = () => {
      this.client.subscribe('/topic/inventory/low-stock', (message: IMessage) => {
        this.subject.next(JSON.parse(message.body));
      });
    };

    this.client.activate();
  }

  private resolveSocketUrl(): string {
    const protocol = window.location.protocol === 'https' ? 'wss' : 'ws';
    return `${protocol}://${window.location.host}/ws`;
  }

  watch(): Observable<LowStockEvent> {
    return this.subject.asObservable();
  }
}

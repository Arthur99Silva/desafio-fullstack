import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Notification {
  id: number;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private counter = 0;
  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  notifications$ = this.notificationsSubject.asObservable();

  success(message: string): void {
    this.show('success', message);
  }

  error(message: string): void {
    this.show('error', message);
  }

  warning(message: string): void {
    this.show('warning', message);
  }

  private show(type: Notification['type'], message: string): void {
    const notification: Notification = {
      id: ++this.counter,
      type,
      message,
    };

    const current = this.notificationsSubject.value;
    this.notificationsSubject.next([...current, notification]);

    setTimeout(() => this.remove(notification.id), 5000);
  }

  remove(id: number): void {
    const current = this.notificationsSubject.value.filter((n) => n.id !== id);
    this.notificationsSubject.next(current);
  }
}

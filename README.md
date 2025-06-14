# ⏰ Alarm-me: Smart Alarm App (Kotlin)

**Alarm-me** is a modern and smart alarm clock app built in **Kotlin**, designed to showcase core Android development skills including alarm scheduling, foreground services, system integrations, and clean architecture.

<img width="371" alt="Screenshot 2025-06-14 at 18 20 37" src="https://github.com/user-attachments/assets/98d0ba9b-1a8b-4211-95ec-e6787983c9fe" />


---

## 🚀 Features

- 🌙 **Dark Mode Support**  
  Responsive UI with system-based light/dark theme adaptation.
  
- 🔔 **Precise Alarm Scheduling**  
  Utilizes `AlarmManager` with support for `setExactAndAllowWhileIdle` and Android 12+ permission handling.

- 📣 **Foreground Services & Notifications**  
  Alarms run in foreground using system-compliant notification actions like **Snooze** and **Cancel**.

- 📦 **Persistent Storage**  
  Saves alarms using `SharedPreferences` with clean architecture.

- ♻️ **Reboot Recovery**  
  Automatically restores alarms after device restart using `BroadcastReceiver`.

- 🔁 **Repeat & Snooze Logic**  
  Supports **daily, weekly**, and **custom repeat** patterns plus smart snoozing.

- 🎵 **Custom Alarm Tones**  
  Optional support for choosing your own sound for each alarm.

- 📳 **Vibration Patterns**  
  Smart, repeatable vibration effects using `VibrationEffect`.

---

## 📸 Screenshots

| Alarm List | Set Alarm | Delete All |
|------------|-----------|--------------|
| <img width="389" alt="Screenshot 2025-06-14 at 18 17 25" src="https://github.com/user-attachments/assets/89d77424-629f-47aa-a4bd-497614cc5697" /> | <img width="367" alt="Screenshot 2025-06-14 at 18 15 45" src="https://github.com/user-attachments/assets/16ff0b81-2205-455c-bf51-d509356f236c" /> | <img width="360" alt="Screenshot 2025-06-14 at 18 18 06" src="https://github.com/user-attachments/assets/c7f5f757-6434-476b-ae34-e30bd9a5dbcf" /> |

---

## 🧠 Architecture

- `AlarmManager` and `PendingIntent` for scheduling
- `SharedPreferences` for lightweight local persistence
- `BroadcastReceiver` for boot recovery
- `ForegroundService` for Android 13+ compliance
- Modular utility classes for vibration, alarm tone, and time formatting

---

## 📱 Tech Stack

| Layer          | Library / API                        |
|----------------|--------------------------------------|
| Language       | Kotlin                               |
| UI Framework   | Android XML Views                    |
| Architecture   | MVVM                                 |
| Storage        | SharedPreferences                    |
| System APIs    | AlarmManager, ForegroundService, Vibrator, BroadcastReceiver |
| Target SDK     | 33 (fully tested on Android 13 & 14). Still works on older version too |

---

## 🔐 Permissions Used

- `POST_NOTIFICATIONS` – For showing alarm notifications
- `SCHEDULE_EXACT_ALARM` – Required for precise timing (Android 12+)
- `FOREGROUND_SERVICE` – For running the alarm in foreground
- `RECEIVE_BOOT_COMPLETED` – To reschedule alarms after reboot
- `VIBRATE` – To support vibration-based alarms

---

## 💡 What's Next

- [ ] Room database integration for alarm storage  
- [ ] Smart dismiss (e.g. math challenge to stop alarm)  
- [ ] Custom alarm tone picker from local storage  
- [ ] Integration with calendar/reminders

---

## 🛠️ Getting Started

1. Clone this repository:
```bash
git clone https://github.com/etineh/alarm-me.git
```
2. Open the project in Android Studio.

3. Build and run on a physical device or emulator.

---

⭐️ Show Your Support
If you found this project helpful or inspiring:

⭐️ Star this repo

📥 Share with fellow devs

🧑‍💼 Use it in your own projects

---

📄 License
MIT License – feel free to use and modify!

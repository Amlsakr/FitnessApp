# Product Brief: Fitness App— AI Personal Health Coach

---

## 1. Overview
**Name:** Fitness App— AI Personal Health Coach
**Platform:** Android (API 26+, target API 35)
**Developer:** Solo (4 years Android experience)
**Technology Stack:** Kotlin, Jetpack Compose, MVI, Coroutines, Room, Retrofit, Firebase, Hilt
**Timeline:** 8 weeks to MVP (v1.0)
**Primary Market:** Egypt / MENA
**UI Language:** English (launch)
**Monetization:** Free at launch, no ads

---

## 2. Market Context & Opportunity
- **Target Demographic:** Young (18‑35) urban users in Egypt; high Android market share (≈84%).
- **Key Pain‑points:** Small workout spaces, poor lighting, subscription fatigue, lack of localized content (Arabic, Ramadan), limited social sharing channels.
- **Competitive Landscape:** Global players (MyFitnessPal, Strava) lack local cultural integration. Local leader **ElCoach** provides Arabic content but misses advanced AI, audio‑first guidance, WhatsApp‑native sharing.
- **Strategic Insight:** An **app‑first Android experience** combined with **viral WhatsApp badge sharing** creates a defensive moat and leverages the dominant communication channel in the region.

---

## 3. Target User Segments
| Segment | Age | Core Needs |
|---|---|---|
| **Social Urbanite** (Segment A) | 18‑25 | Community‑driven progress, easy sharing, social challenges |
| **Convenience‑Seeking Professional** (Segment B) | 25‑35 | Quick home workouts, AI personalization, minimal setup |
| **Culturally‑Conscious Health Seeker** (Segment C) | 20‑40 (primarily female) | Privacy, Arabic support, modest workout environments |

---

## 4. MVP Scope (v1.0)
### Hard Core (Weeks 1‑6 – must‑have for launch)
- Differentiated onboarding (Beginner / Intermediate paths)
- AI‑generated workout plans (Gemini API)
- Guided workout sessions & exercise library
- Pose detection + form feedback (ML Kit)
- Lighting fallback mode (audio when dark)
- Fatigue breakdown detection
- Progress analytics + charts
- Firebase Auth + user profile

### Nice‑to‑Have (Weeks 7‑8 – optional if time permits)
- Spatial audio coach (hands‑free guidance)
- Discreet gym mode (minimal UI + vibration)
- Dynamic equipment rerouting (one‑tap alternatives)
- Anonymous team challenges (Firestore backend)
- WhatsApp badge sharing (Android share‑sheet)
- Small‑space workout mode (< 2 m × 2 m)
- Home screen widget (Jetpack Glance)
- Smart reminders (WorkManager)

---

## 5. Deferred Features (v1.1)
- Wear OS companion app
- Nutrition logging & barcode scanner
- Health Connect integration
- Mirror detection & fallback
- B2B Trainer Dashboard
- Branded/sponsored challenges
- Ramadan‑specific mode & scheduling
- Arabic language support
- One‑time analytics unlock (EGP 49)
- Local payment integrations (Fawry, Vodafone Cash)

---

## 6. Key Differentiators (validated by research)
1. **Audio fallback** for poor lighting/low bandwidth
2. **Spatial audio coach** – hands‑free guidance
3. **Fatigue breakdown detection** – proactive rest recommendations
4. **Discreet gym mode** – vibration‑only UI for privacy
5. **Dynamic equipment rerouting** – one‑tap alternatives
6. **Anonymous team challenges** – community without exposing identity
7. **WhatsApp badge sharing** – viral growth loop, no bot required
8. **Small‑space workouts** – fits typical Egyptian apartments

---

## 7. Success Metrics (3 months post‑launch)
- **Active Users:** ≥ 1,000
- **Play Store Rating:** ≥ 4.0 stars
- **30‑day Retention:** ≥ 40 %
- **WhatsApp badge shares:** ≥ 200 shares in first 30 days post‑launch
- **Session completion rate:** ≥ 60 % (users who start a workout finish it)
- **Onboarding completion:** ≥ 80 % (users who open app complete setup)
- **Pose detection satisfaction:** ≥ 4/5 in‑session rating

---

## 8. Risks & Mitigations
| Risk | Mitigation |
|---|---|
| **ML Kit pose‑detection accuracy on mid‑range Egyptian devices** | Conduct extensive device‑matrix testing; fallback to audio mode when confidence < 0.6 |
| **Gemini API latency** | Cache generated plans locally; pre‑warm requests; graceful degradation with static templates |
| **8‑week timeline for 16 features** | Prioritize hard‑core MVP; use agile sprints with weekly demos |
| **Play Store camera permission rejections** | Provide clear privacy policy, limit camera usage to pose detection only, request permission on‑demand |

---

## 9. Next Steps (Analyst Hand‑off)
- Review the brief for any missing stakeholder assumptions.
- Refine success metrics with concrete tracking plans.
- Align on detailed MVP roadmap and sprint schedule.
- Identify any additional regulatory or privacy reviews needed.

---

*Prepared for hand‑off to the BMAD Analyst agent.*

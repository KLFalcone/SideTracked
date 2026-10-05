# SideTracked

> **Status:** Active development (as of Oct. 5, 2026)

Turn getting sidetracked into progress.

SideTracked is a gamified curiosity and productivity platform built around a simple idea:

**Getting sidetracked isn't always a bad thing.**

Part task manager, part curiosity engine, SideTracked turns everyday tasks into quests while giving curiosity somewhere useful to go.

Create quests. Earn XP. Follow the white rabbit. Find something strange.

---

## What is SideTracked?

SideTracked currently has two interconnected systems.

### Quest System

Everyday tasks become quests with difficulty levels and XP rewards.

Users can:

- Create quests
- Assign quest types and difficulty
- Complete quests for XP
- Track level progression
- Maintain a completed quest archive

The goal is to make getting things done feel a little less like maintaining a to-do list and a little more like progressing through a game.

### Curiosity Engine

Sometimes you don't need another task.

You need a rabbit hole.

**Get Sidetracked** sends the user into a randomly selected topic designed to spark exploration, research, experimentation, or learning.

A rabbit hole can include:

- A topic worth exploring
- A short hook
- A research question
- Related subject areas
- Starting sources
- XP rewards
- Personal research notes
- Saved discoveries

The long-term goal is to make rabbit holes dynamic enough that curiosity never becomes another repetitive feed.

---

## Follow the White Rabbit

The Curiosity Engine is organized around broad "Worlds" rather than a small collection of hardcoded prompts.

Current and planned Worlds include:

- Science & Experiments
- Atomic Age
- History & Lost Knowledge
- Technology & Engineering
- Manufacturing & Industry
- Transportation & Infrastructure
- Architecture & Built World
- Energy & Power
- Agriculture & Food Systems
- Medicine
- Space & The Unknown
- Exploration & Expeditions
- Roads Less Traveled
- Making & Fabrication
- Trades & Forgotten Skills
- Trading Card Games
- Food & Cooking
- Music & Sound
- Books & Archives
- Earth & Materials
- Reality & Simulation
- Signals & Communications
- Wildcard

Topics can cross multiple Worlds.

A rabbit hole about Conway's Game of Life, for example, might connect Reality & Simulation, computing, mathematics, science, and history.

That's intentional.

Curiosity rarely stays in one category.

---

## Roads Less Traveled

Some rabbit holes leave the browser entirely.

Roads Less Traveled is being designed around discovering unusual places and forgotten history nearby:

- Abandoned and forgotten places
- Industrial history
- Mines and geology
- Cold War and military sites
- Local legends
- Strange roads
- Roadside oddities
- Historic infrastructure
- Geocaching
- Architecture
- Natural oddities

Future location-based discovery will support adjustable search distances ranging from nearby exploration to full road trips.

SideTracked encourages legal, responsible exploration: respect property boundaries, leave places as you found them, take nothing, and leave no trace.

---

## Personal Archive

Interesting things shouldn't disappear because you closed a tab.

SideTracked is being built with a personal research archive where rabbit holes can be saved for later and eventually expanded with:

- Notes
- Research findings
- Sources
- Photos
- Videos
- Saved locations
- Tags
- Exploration status

The eventual goal is to turn scattered curiosity into a personal library of things you've learned, found, built, visited, or still want to investigate.

---

## Community

A future community layer will allow users to share discoveries without turning SideTracked into another traditional social media platform.

Think less:

**"Build a following."**

And more:

**"I found something weird. Does anyone else know what this is?"**

Research findings, historical discoveries, projects, photos, videos, and exploration reports could become shared rabbit holes for other users to continue.

---

## Tech Stack

### Frontend

- Angular
- TypeScript
- HTML
- CSS

### Backend

- Java
- Spring Boot
- Spring Data JPA
- REST APIs

### Database

- MySQL

### Current Architecture

```text
Angular Frontend
       |
       | HTTP / JSON
       v
Spring Boot REST API
       |
       | JPA / Hibernate
       v
     MySQL
```

---

## Project Structure

```text
SideTracked/
|
|-- backend/
|   `-- sidetracked-api/    Spring Boot REST API
|
|-- frontend/               Angular application
|
|-- docs/                   Architecture and project documentation
|
|-- .gitignore
`-- README.md
```

---

## Current Status

SideTracked is under active development.

Currently implemented:

- Quest creation
- Quest completion
- XP rewards
- Level progression
- Persistent MySQL storage
- Rabbit hole discovery API
- Rabbit hole persistence
- Curiosity Engine discovery flow
- Personal rabbit hole archive

In development / planned:

- Dynamic rabbit hole generation
- Real research sources and external links
- World and tag architecture
- Improved rabbit hole presentation
- Save-for-later workflow
- Research notes
- Discovery history
- Roads Less Traveled
- Location-aware exploration
- Community discoveries
- Photo and video sharing

---

## Development

SideTracked currently runs as separate frontend and backend applications.

### Backend

From:

```text
backend/sidetracked-api
```

Run:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw spring-boot:run
```

The API runs locally on:

```text
http://localhost:8080
```

### Frontend

From:

```text
frontend
```

Run:

```bash
npm install
npm start
```

The Angular application runs locally on:

```text
http://localhost:4200
```

### Database

The backend currently expects a local MySQL database named:

```text
sidetracked
```

Database credentials should be provided through environment variables and should never be committed to the repository.

---

## The Idea

The internet is extremely good at getting people sidetracked.

SideTracked asks:

**What if getting sidetracked actually led somewhere?**

Not another infinite feed.

Not another productivity dashboard.

Just a trail of interesting things worth following.

`FOLLOW THE WHITE RABBIT`

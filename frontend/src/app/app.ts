import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';


interface Quest {
  id?: number;
  title: string;
  description: string;
  type: string;
  priority: string;
  completed: boolean;
  xp: number;
}


interface Player {
  id: number;
  xp: number;
  questsCompleted: number;
  rabbitHolesCompleted: number;
}


interface RabbitHoleSource {
  label: string;
  url: string;
  sourceType: string;
}


interface World {
  slug: string;
  name: string;
  tagline: string;
  topicCount: number;
}


interface RabbitHole {
  id: number;
  world: string;
  relatedWorlds: string[];
  title: string;
  hook: string;
  researchPrompt: string;
  xp: number;

  tags: string[];
  sources: RabbitHoleSource[];

  status:
    | 'DISCOVERED'
    | 'SAVED'
    | 'EXPLORING'
    | 'COMPLETED';

  discoveredAt: string;

  reflection?: string | null;
}


@Component({
  selector: 'app-root',
  imports: [FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {

  // =========================
  // API ENDPOINTS
  // =========================

  /*
   * The one place the backend address is set.
   */
  private apiBaseUrl =
    'http://localhost:8080/api';

  private questApiUrl =
    `${this.apiBaseUrl}/quests`;

  private playerApiUrl =
    `${this.apiBaseUrl}/player`;

  private rabbitHoleApiUrl =
    `${this.apiBaseUrl}/rabbit-holes`;

  private worldApiUrl =
    `${this.apiBaseUrl}/worlds`;


  // =========================
  // CONNECTION
  // =========================

  /*
   * True when the backend couldn't be reached,
   * so the page can say so instead of looking empty.
   */
  connectionError = signal(false);

  questError = signal<string | null>(null);


  // =========================
  // PLAYER
  // =========================

  player = signal<Player>({
    id: 0,
    xp: 0,
    questsCompleted: 0,
    rabbitHolesCompleted: 0
  });


  get xp(): number {
    return this.player().xp;
  }


  get completedQuestCount(): number {
    return this.player().questsCompleted;
  }


  get level(): number {

    if (this.xp < 100) return 1;
    if (this.xp < 250) return 2;
    if (this.xp < 450) return 3;
    if (this.xp < 700) return 4;
    if (this.xp < 1000) return 5;

    return 6 + Math.floor(
      (this.xp - 1000) / 350
    );
  }


  get currentLevelStartXp(): number {

    const thresholds = [
      0,
      100,
      250,
      450,
      700,
      1000
    ];

    if (this.level <= 6) {
      return thresholds[this.level - 1];
    }

    return (
      1000 +
      (this.level - 6) * 350
    );
  }


  get nextLevelXp(): number {

    const thresholds = [
      100,
      250,
      450,
      700,
      1000
    ];

    if (this.level <= 5) {
      return thresholds[this.level - 1];
    }

    return (
      1000 +
      (this.level - 5) * 350
    );
  }


  get levelProgress(): number {

    const earnedThisLevel =
      this.xp -
      this.currentLevelStartXp;

    const neededThisLevel =
      this.nextLevelXp -
      this.currentLevelStartXp;

    return Math.min(
      100,
      Math.max(
        0,
        (
          earnedThisLevel /
          neededThisLevel
        ) * 100
      )
    );
  }


  // =========================
  // QUESTS
  // =========================

  quests = signal<Quest[]>([]);

  newQuestTitle = '';
  newQuestDescription = '';

  newQuestType =
    'SELF-CARE';


  /*
   * Mirrors the XP rules in the backend QuestService
   * (20 base XP x the type's multiplier). The backend
   * calculates the real XP; these are for display only.
   */
  questTypes = [
    {
      name: 'QUICK WIN',
      xp: 10,
      hint: 'A tiny task, under 10 minutes.',
      example: 'Get a glass of water.'
    },
    {
      name: 'SELF-CARE',
      xp: 20,
      hint: 'Looking after yourself.',
      example: 'Take your meds.'
    },
    {
      name: 'CARETAKER',
      xp: 25,
      hint: 'Looking after pets or people.',
      example: 'Feed the pets.'
    },
    {
      name: 'CHORE',
      xp: 30,
      hint: 'House, errands, and admin.',
      example: 'Do a load of laundry.'
    },
    {
      name: 'MAIN QUEST',
      xp: 40,
      hint: 'The important thing today.',
      example: 'Finish the assignment.'
    },
    {
      name: 'BOSS FIGHT',
      xp: 60,
      hint: 'The task you have been avoiding.',
      example: 'Call the dentist.'
    }
  ];


  get newQuestTypeInfo() {

    return this.questTypes.find(
      type =>
        type.name ===
        this.newQuestType
    );
  }


  // =========================
  // RABBIT HOLES
  // =========================

  currentRabbitHole =
    signal<RabbitHole | null>(null);

  rabbitHoleLibrary =
    signal<RabbitHole[]>([]);

  followingRabbit = signal(false);

  rabbitHoleReflection = '';

  discoveryMessage = signal<string | null>(null);


  // =========================
  // WORLDS
  // =========================

  worlds = signal<World[]>([]);

  discoveryMode:
    'surprise' | 'world' = 'surprise';

  selectedWorld = '';


  get selectedWorldInfo(): World | undefined {

    return this.worlds().find(
      world =>
        world.slug ===
        this.selectedWorld
    );
  }


  // =========================
  // STARTUP
  // =========================

  constructor(
    private http: HttpClient
  ) {
    this.loadAllPersistentState();
  }


  loadAllPersistentState(): void {

    this.connectionError.set(false);

    this.loadPlayer();
    this.loadQuests();
    this.loadRabbitHoleLibrary();
    this.loadWorlds();
  }


  loadWorlds(): void {

    this.http
      .get<World[]>(
        this.worldApiUrl
      )
      .subscribe({

        next: worlds => {

          this.worlds.set(worlds);
        },

        error: error => {

          this.connectionError.set(true);

          console.error(
            'Failed to load Worlds.',
            error
          );
        }

      });
  }


  // =========================
  // PLAYER
  // =========================

  loadPlayer(): void {

    this.http
      .get<Player>(
        this.playerApiUrl
      )
      .subscribe({

        next: player => {

          this.player.set(player);

          console.log(
            'Player loaded:',
            player
          );
        },

        error: error => {

          this.connectionError.set(true);

          console.error(
            'Failed to load player.',
            error
          );
        }

      });
  }


  // =========================
  // QUEST LOADING
  // =========================

  loadQuests(): void {

    this.http
      .get<Quest[]>(
        this.questApiUrl
      )
      .subscribe({

        next: data => {

          this.quests.set(data);

          console.log(
            'Quests loaded:',
            data
          );
        },

        error: error => {

          this.connectionError.set(true);

          console.error(
            'Failed to load quests.',
            error
          );
        }

      });
  }


  // =========================
  // CREATE QUEST
  // =========================

  createQuest(): void {

    if (
      !this.newQuestTitle.trim()
    ) {
      return;
    }

    this.questError.set(null);


    const quest: Quest = {

      title:
        this.newQuestTitle,

      description:
        this.newQuestDescription,

      type:
        this.newQuestType,

      priority:
        '',

      completed:
        false,

      /*
       * Replaced by the backend's calculation.
       */
      xp:
        0
    };


    this.http
      .post<Quest>(
        this.questApiUrl,
        quest
      )
      .subscribe({

        next: savedQuest => {

          this.quests.update(
            quests => [...quests, savedQuest]
          );

          this.newQuestTitle = '';
          this.newQuestDescription = '';

          this.newQuestType =
            'SELF-CARE';

        },

        error: error => {

          this.questError.set(
            error.status === 400
              ? 'That quest couldn\'t be saved. Check the title isn\'t empty and the title and notes are under 255 characters.'
              : 'That quest couldn\'t be saved. Is the server running?'
          );

          console.error(
            'Failed to create quest.',
            error
          );
        }

      });
  }


  // =========================
  // COMPLETE QUEST
  // =========================

  completeQuest(
    quest: Quest
  ): void {

    if (
      quest.completed ||
      !quest.id
    ) {
      return;
    }


    this.http
      .put<Quest>(
        `${this.questApiUrl}/${quest.id}/complete`,
        {}
      )
      .subscribe({

        next: savedQuest => {

          this.quests.update(
            quests => quests.map(
              q =>
                q.id === savedQuest.id
                  ? savedQuest
                  : q
            )
          );


          /*
           * Completing a quest changes XP,
           * so refresh the player afterward.
           */
          this.loadPlayer();

        },

        error: error => {

          console.error(
            'Failed to complete quest.',
            error
          );
        }

      });
  }


  // =========================
  // DELETE QUEST
  // =========================

  deleteQuest(
    quest: Quest
  ): void {

    if (!quest.id) {
      return;
    }


    this.http
      .delete(
        `${this.questApiUrl}/${quest.id}`
      )
      .subscribe({

        next: () => {

          this.quests.update(
            quests => quests.filter(
              q =>
                q.id !==
                quest.id
            )
          );

          /*
           * A deleted completed quest may
           * affect progression totals.
           */
          this.loadPlayer();

        },

        error: error => {

          console.error(
            'Failed to delete quest.',
            error
          );
        }

      });
  }


  // =========================
  // CURIOSITY ENGINE
  // =========================

  getSidetracked(): void {

    if (this.followingRabbit()) {
      return;
    }


    const worldSlug =
      this.discoveryMode === 'world'
        ? this.selectedWorld
        : '';

    if (
      this.discoveryMode === 'world' &&
      !worldSlug
    ) {
      return;
    }


    this.followingRabbit.set(true);
    this.currentRabbitHole.set(null);
    this.discoveryMessage.set(null);
    this.rabbitHoleReflection = '';


    /*
     * "Surprise Me" sends no World, so any World can come up.
     */
    const params: Record<string, string> =
      worldSlug
        ? { world: worldSlug }
        : {};


    this.http
      .post<RabbitHole>(
        `${this.rabbitHoleApiUrl}/discover`,
        {},
        { params }
      )
      .subscribe({

        next: rabbitHole => {

          console.log(
            'Rabbit Hole received:',
            rabbitHole
          );


          this.currentRabbitHole.set(
            rabbitHole
          );


          this.followingRabbit.set(
            false
          );


          this.loadRabbitHoleLibrary();

        },


        error: error => {

          console.error(
            'Rabbit Hole discovery failed.',
            error
          );


          this.discoveryMessage.set(
            error.status === 404
              ? 'The rabbit found nothing in this World yet. Try another one.'
              : 'The rabbit got lost. Is the server running?'
          );


          this.followingRabbit.set(
            false
          );

        }

      });
  }


  // =========================
  // RABBIT HOLE LIBRARY
  // =========================

  loadRabbitHoleLibrary(): void {

    this.http
      .get<RabbitHole[]>(
        this.rabbitHoleApiUrl
      )
      .subscribe({

        next: rabbitHoles => {

          /*
           * DISCOVERED means the user has
           * seen the Rabbit Hole but hasn't
           * intentionally saved or explored it.
           *
           * Only Rabbit Holes the user chose
           * to keep belong in the archive.
           */
          this.rabbitHoleLibrary.set(
            rabbitHoles.filter(
              rabbitHole =>
                rabbitHole.status !==
                'DISCOVERED'
            )
          );

          console.log(
            'Rabbit Hole library loaded:',
            this.rabbitHoleLibrary()
          );
        },

        error: error => {

          this.connectionError.set(true);

          console.error(
            'Failed to load Rabbit Hole library.',
            error
          );
        }

      });
  }


  // =========================
  // SAVE RABBIT HOLE
  // =========================

  saveRabbitHole(): void {

    const currentRabbitHole =
      this.currentRabbitHole();

    if (
      !currentRabbitHole
    ) {
      return;
    }


    this.http
      .put<RabbitHole>(
        `${this.rabbitHoleApiUrl}/${currentRabbitHole.id}/save`,
        {}
      )
      .subscribe({

        next: rabbitHole => {

          this.currentRabbitHole.set(
            rabbitHole
          );

          this.loadRabbitHoleLibrary();

        },

        error: error => {

          console.error(
            'Failed to save Rabbit Hole.',
            error
          );
        }

      });
  }


  // =========================
  // EXPLORE RABBIT HOLE
  // =========================

  exploreRabbitHole(): void {

    const currentRabbitHole =
      this.currentRabbitHole();

    if (
      !currentRabbitHole
    ) {
      return;
    }


    this.http
      .put<RabbitHole>(
        `${this.rabbitHoleApiUrl}/${currentRabbitHole.id}/explore`,
        {}
      )
      .subscribe({

        next: rabbitHole => {

          this.currentRabbitHole.set(
            rabbitHole
          );

          this.loadRabbitHoleLibrary();

        },

        error: error => {

          console.error(
            'Failed to start exploring Rabbit Hole.',
            error
          );
        }

      });
  }


  // =========================
  // COMPLETE RABBIT HOLE
  // =========================

  completeRabbitHole(): void {

    const currentRabbitHole =
      this.currentRabbitHole();

    if (
      !currentRabbitHole
    ) {
      return;
    }


    this.http
      .put<RabbitHole>(
        `${this.rabbitHoleApiUrl}/${currentRabbitHole.id}/complete`,
        {
          reflection:
            this.rabbitHoleReflection
        }
      )
      .subscribe({

        next: rabbitHole => {

          this.currentRabbitHole.set(
            rabbitHole
          );

          this.rabbitHoleReflection = '';


          /*
           * Completing a Rabbit Hole awards XP,
           * so refresh the player afterward.
           */
          this.loadPlayer();

          this.loadRabbitHoleLibrary();

        },

        error: error => {

          console.error(
            'Failed to complete Rabbit Hole.',
            error
          );
        }

      });
  }


  // =========================
  // OPEN ARCHIVED RABBIT HOLE
  // =========================

  openRabbitHole(
    rabbitHole: RabbitHole
  ): void {

    this.currentRabbitHole.set(
      rabbitHole
    );

    this.rabbitHoleReflection = '';


    /*
     * For now this returns the user
     * to the Curiosity Engine area.
     *
     * Later Rabbit Holes can have
     * their own dedicated research view.
     */
    setTimeout(() => {

      const element =
        document.querySelector(
          '.curiosity-engine'
        );

      element?.scrollIntoView({
        behavior: 'smooth',
        block: 'start'
      });

    }, 0);
  }


  // =========================
  // DISPLAY HELPERS
  // =========================

  formatRabbitHoleId(
    id: number
  ): string {

    return id
      .toString()
      .padStart(
        4,
        '0'
      );
  }

}
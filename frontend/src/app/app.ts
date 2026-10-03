import { Component } from '@angular/core';
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
  encountersCompleted: number;
}


interface RabbitHole {
  id: number;
  world: string;
  title: string;
  hook: string;
  researchPrompt: string;
  xp: number;

  status:
    | 'DISCOVERED'
    | 'SAVED'
    | 'EXPLORING'
    | 'COMPLETED';

  discoveredAt: string;
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

  private questApiUrl =
    'http://localhost:8080/api/quests';

  private playerApiUrl =
    'http://localhost:8080/api/player';

  private rabbitHoleApiUrl =
    'http://localhost:8080/api/rabbit-holes';


  // =========================
  // PLAYER
  // =========================

  player: Player = {
    id: 0,
    xp: 0,
    questsCompleted: 0,
    encountersCompleted: 0
  };


  get xp(): number {
    return this.player.xp;
  }


  get completedQuestCount(): number {
    return this.player.questsCompleted;
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

  quests: Quest[] = [];

  newQuestTitle = '';
  newQuestDescription = '';

  newQuestType =
    'SIDE QUEST';

  newQuestDifficulty =
    'normal';


  // =========================
  // RABBIT HOLES
  // =========================

  currentRabbitHole:
    RabbitHole | null = null;

  rabbitHoleLibrary:
    RabbitHole[] = [];

  followingRabbit = false;


  // =========================
  // STARTUP
  // =========================

  constructor(
    private http: HttpClient
  ) {

    this.loadPlayer();
    this.loadQuests();
    this.loadRabbitHoleLibrary();
  }


  // =========================
  // PLAYER
  // =========================

  loadPlayer() {

    this.http
      .get<Player>(
        this.playerApiUrl
      )
      .subscribe(player => {

        this.player = player;

      });
  }


  // =========================
  // QUESTS
  // =========================

  loadQuests() {

    this.http
      .get<Quest[]>(
        this.questApiUrl
      )
      .subscribe(data => {

        this.quests = data;

      });
  }


  createQuest() {

    if (
      !this.newQuestTitle.trim()
    ) {
      return;
    }


    const xpRewards:
      Record<string, number> = {

        easy: 10,
        normal: 20,
        hard: 35,
        legendary: 50

      };


    const quest: Quest = {

      title:
        this.newQuestTitle,

      description:
        this.newQuestDescription,

      type:
        this.newQuestType,

      priority:
        this.newQuestDifficulty,

      completed:
        false,

      xp:
        xpRewards[
          this.newQuestDifficulty
        ]
    };


    this.http
      .post<Quest>(
        this.questApiUrl,
        quest
      )
      .subscribe(savedQuest => {

        this.quests.push(
          savedQuest
        );

        this.newQuestTitle = '';
        this.newQuestDescription = '';

        this.newQuestType =
          'SIDE QUEST';

        this.newQuestDifficulty =
          'normal';

      });
  }


  completeQuest(
    quest: Quest
  ) {

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
      .subscribe(savedQuest => {

        const index =
          this.quests.findIndex(
            q =>
              q.id ===
              savedQuest.id
          );


        if (index !== -1) {

          this.quests[index] =
            savedQuest;

        }


        this.loadPlayer();

      });
  }


  deleteQuest(
    quest: Quest
  ) {

    if (!quest.id) {
      return;
    }


    this.http
      .delete(
        `${this.questApiUrl}/${quest.id}`
      )
      .subscribe(() => {

        this.quests =
          this.quests.filter(
            q =>
              q.id !==
              quest.id
          );

        this.loadPlayer();

      });
  }


  // =========================
  // CURIOSITY ENGINE
  // =========================

  getSidetracked() {

    if (this.followingRabbit) {
      return;
    }


    this.followingRabbit = true;
    this.currentRabbitHole = null;


    this.http
      .post<RabbitHole>(
        `${this.rabbitHoleApiUrl}/discover`,
        {}
      )
      .subscribe({

        next: rabbitHole => {

          console.log(
            'Rabbit Hole received:',
            rabbitHole
          );


          this.currentRabbitHole =
            rabbitHole;


          this.followingRabbit =
            false;


          this.loadRabbitHoleLibrary();

        },


        error: error => {

          console.error(
            'Rabbit Hole discovery failed.',
            error
          );


          this.followingRabbit =
            false;

        }

      });
  }


  // =========================
  // RABBIT HOLE LIBRARY
  // =========================

  loadRabbitHoleLibrary() {

    this.http
      .get<RabbitHole[]>(
        this.rabbitHoleApiUrl
      )
      .subscribe(rabbitHoles => {

        /*
         * DISCOVERED means the user
         * has seen the Rabbit Hole,
         * but has not chosen to keep it.
         *
         * The archive contains Rabbit
         * Holes they intentionally saved
         * or explored.
         */

        this.rabbitHoleLibrary =
          rabbitHoles.filter(
            rabbitHole =>
              rabbitHole.status !==
              'DISCOVERED'
          );

      });
  }


  saveRabbitHole() {

    if (
      !this.currentRabbitHole
    ) {
      return;
    }


    this.http
      .put<RabbitHole>(
        `${this.rabbitHoleApiUrl}/${this.currentRabbitHole.id}/save`,
        {}
      )
      .subscribe(rabbitHole => {

        this.currentRabbitHole =
          rabbitHole;

        this.loadRabbitHoleLibrary();

      });
  }


  exploreRabbitHole() {

    if (
      !this.currentRabbitHole
    ) {
      return;
    }


    this.http
      .put<RabbitHole>(
        `${this.rabbitHoleApiUrl}/${this.currentRabbitHole.id}/explore`,
        {}
      )
      .subscribe(rabbitHole => {

        this.currentRabbitHole =
          rabbitHole;

        this.loadRabbitHoleLibrary();

      });
  }


  openRabbitHole(
    rabbitHole: RabbitHole
  ) {

    this.currentRabbitHole =
      rabbitHole;


    /*
     * For now this returns the user
     * to the Curiosity Engine area.
     *
     * Later Rabbit Holes will get
     * their own full research view.
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
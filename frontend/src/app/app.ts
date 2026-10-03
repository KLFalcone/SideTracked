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

interface Encounter {
  title: string;
  mission: string;
  xp: number;
}

@Component({
  selector: 'app-root',
  imports: [FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {

  // =========================
  // PLAYER
  // =========================

  xp = 0;


  // =========================
  // QUESTS
  // =========================

  quests: Quest[] = [];

  newQuestTitle = '';
  newQuestDescription = '';
  newQuestType = '⚔️ SIDE QUEST';
  newQuestDifficulty = 'normal';

  private apiUrl = 'http://localhost:8080/api/quests';

  constructor(private http: HttpClient) {
    this.loadQuests();
  }

  loadQuests() {
    this.http.get<Quest[]>(this.apiUrl).subscribe(data => {
      this.quests = data;
      this.calculateXp();
    });
  }

  createQuest() {
    if (!this.newQuestTitle.trim()) {
      return;
    }

    const xpRewards: Record<string, number> = {
      easy: 10,
      normal: 20,
      hard: 35,
      legendary: 50
    };

    const quest: Quest = {
      title: this.newQuestTitle,
      description: this.newQuestDescription,
      type: this.newQuestType,
      priority: this.newQuestDifficulty,
      completed: false,
      xp: xpRewards[this.newQuestDifficulty]
    };

    this.http.post<Quest>(this.apiUrl, quest).subscribe(savedQuest => {
      this.quests.push(savedQuest);

      this.newQuestTitle = '';
      this.newQuestDescription = '';
      this.newQuestType = '⚔️ SIDE QUEST';
      this.newQuestDifficulty = 'normal';
    });
  }

  completeQuest(quest: Quest) {
    if (quest.completed || !quest.id) {
      return;
    }

    const updatedQuest = {
      ...quest,
      completed: true
    };

    this.http
      .put<Quest>(`${this.apiUrl}/${quest.id}`, updatedQuest)
      .subscribe(savedQuest => {

        const index = this.quests.findIndex(q => q.id === savedQuest.id);

        if (index !== -1) {
          this.quests[index] = savedQuest;
        }

        this.calculateXp();
      });
  }

  deleteQuest(quest: Quest) {
    if (!quest.id) {
      return;
    }

    this.http
      .delete(`${this.apiUrl}/${quest.id}`)
      .subscribe(() => {
        this.quests = this.quests.filter(q => q.id !== quest.id);
        this.calculateXp();
      });
  }

  calculateXp() {
    this.xp = this.quests
      .filter(quest => quest.completed)
      .reduce((total, quest) => total + quest.xp, 0);
  }


  // =========================
  // RANDOM ENCOUNTERS
  // =========================

  encounters: Encounter[] = [
    {
      title: '🧪 Science Rabbit Hole',
      mission: 'Learn one weird science fact you did not know 5 minutes ago.',
      xp: 15
    },
    {
      title: '🐈 Feline Inspection',
      mission: 'Locate the nearest cat. Give appropriate tribute. Return immediately.',
      xp: 10
    },
    {
      title: '🎴 Professor Oak Requires Data',
      mission: 'Pick one Pokémon card nearby and learn one completely unnecessary fact about it.',
      xp: 15
    },
    {
      title: '☢️ Forbidden Glass',
      mission: 'Find the coolest piece of uranium glass in your collection. Admire responsibly.',
      xp: 10
    },
    {
      title: '🧠 Interview Jumpscare',
      mission: 'Answer one junior developer interview question. No Googling.',
      xp: 25
    },
    {
      title: '🌀 Wombat Incident',
      mission: 'You already know what you did.',
      xp: 5
    }
  ];

  currentEncounter: Encounter | null = null;

  getSidetracked() {
    const randomIndex = Math.floor(Math.random() * this.encounters.length);
    this.currentEncounter = this.encounters[randomIndex];
  }

  completeEncounter() {
    if (this.currentEncounter) {
      this.xp += this.currentEncounter.xp;
      this.currentEncounter = null;
    }
  }

  dismissEncounter() {
    this.currentEncounter = null;
  }
}
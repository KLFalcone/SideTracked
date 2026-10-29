package com.sidetracked.api.model;

/*
 * What caused an XP change. Together with an XpEvent's sourceId it
 * identifies the exact thing that earned or removed the XP.
 */
public enum XpSourceType {

    /* XP earned before XP history existed. sourceId is 0. */
    LEGACY,

    /* sourceId is the QuestCompletion id. */
    QUEST_COMPLETED,

    /* A completed quest was deleted. sourceId is the QuestCompletion id. */
    QUEST_DELETED,

    /* A completion was undone. sourceId is the QuestCompletion id. */
    COMPLETION_UNDONE,

    /* sourceId is the UserRabbitHole id. */
    RABBIT_HOLE_COMPLETED
}

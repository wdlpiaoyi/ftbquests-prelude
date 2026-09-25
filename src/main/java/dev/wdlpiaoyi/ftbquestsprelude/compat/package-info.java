/**
 * This package is the <b>only</b> place in the mod that may directly reference FTB Quests types.
 *
 * <p>Keeping every FTB Quests reference here isolates the integration from the rest of the mod:
 *
 * <ul>
 *   <li>{@link FTBQuestsCompat} performs availability and version detection without touching
 *       FTB Quests classes at all.</li>
 *   <li>Loader / session classes (added in later phases) wrap FTB Quests data types such as
 *       {@code ClientQuestFile} and {@code QuestScreen}.</li>
 * </ul>
 *
 * <p>Rules for this package:
 * <ul>
 *   <li>Never leak an FTB Quests type through a public signature used by non-compat code.</li>
 *   <li>Never bundle FTB Quests code; FTB Quests is a runtime dependency only.</li>
 *   <li>Guard every entry point with {@link FTBQuestsCompat#canUseLocalQuestBook()}.</li>
 * </ul>
 */
package dev.wdlpiaoyi.ftbquestsprelude.compat;

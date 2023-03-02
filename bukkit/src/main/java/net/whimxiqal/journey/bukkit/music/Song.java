/*
 * MIT License
 *
 * Copyright (c) whimxiqal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.whimxiqal.journey.bukkit.music;

import net.whimxiqal.journey.bukkit.JourneyBukkit;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import lombok.Value;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

/**
 * A class for defining some music to be played to players in Minecraft.
 */
public class Song {

  /**
   * A single arpeggio to symbolize success.
   */
  public static final Song SUCCESS_CHORD = new Song(Arrays.asList(
      new Note(Sound.ENTITY_GLOW_SQUID_AMBIENT, 1.5f, 0, 1.2f)
  ));

  private final List<Note> notes = new LinkedList<>();

  /**
   * General constructor that takes some list of notes.
   *
   * @param notes the notes of the song
   */
  public Song(List<Note> notes) {
    this.notes.addAll(notes);
  }

  /**
   * Add a note to the song.
   *
   * @param note the note
   */
  public void addNode(Note note) {
    this.notes.add(note);
  }

  /**
   * Play the song for a Minecraft player.
   *
   * @param player the player
   */
  public void play(Player player) {
    for (int i = 0; i < notes.size(); i++) {
      playNote(player, i);
    }
  }

  private void playNote(Player player, int index) {
    Bukkit.getScheduler().scheduleSyncDelayedTask(JourneyBukkit.get(), () ->
            player.playSound(player.getLocation(), this.notes.get(index).sound,
                SoundCategory.AMBIENT, this.notes.get(index).volume, this.notes.get(index).pitch),
        this.notes.get(index).delay);
  }

  /**
   * A single sound (note) that can be strung together into a {@link Song}.
   */
  @Value
  @Accessors(fluent = true)
  public static class Note {
    Sound sound;
    float pitch;
    int delay;
    float volume;
  }

}

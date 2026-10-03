package dev.melonclient.gui.hudeditor.impl.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import dev.melonclient.Melon;
import dev.melonclient.gui.hudeditor.HudEditor;
import dev.melonclient.gui.hudeditor.impl.HudMod;
import dev.melonclient.helpers.ResolutionHelper;
import dev.melonclient.helpers.render.GLHelper;
import dev.melonclient.helpers.render.Helper2D;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ScoreboardHud extends HudMod {

    public ScoreboardHud(String name, int x, int y) {
        super(name, x, y);
    }

    @Override
    public void renderMod(int mouseX, int mouseY) {
        GLHelper.startScale(getX(), getY(), getSize());
        if (Melon.INSTANCE.modManager.getMod(getName()).isToggled()) {
            drawScoreboardPlaceHolder(isBackground(), isRemoveNumbers());
            super.renderMod(mouseX, mouseY);
        }
        GLHelper.endScale();
    }

    @SubscribeEvent
    public void onRender2D(RenderGameOverlayEvent.Pre.Text e) {
        GLHelper.startScale(getX(), getY(), getSize());
        if (Melon.INSTANCE.modManager.getMod(getName()).isToggled() && !(Melon.INSTANCE.mc.currentScreen instanceof HudEditor)) {
            ScoreObjective scoreobjective = Melon.INSTANCE.mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);
            if (scoreobjective != null) {
                drawScoreboard(scoreobjective, isBackground(), isRemoveNumbers());
            }
        }
        GLHelper.endScale();
    }

    /**
     * Przesuwa x tak, żeby box scoreboardu (o szerokości displayWidth, w LOKALNEJ
     * przestrzeni transformacji GLHelper.startScale) nigdy nie wystawał poza prawą
     * krawędź ekranu. Szerokość scoreboardu jest zmienna zależnie od serwera
     * (długość nazw graczy/drużyn/wyniku), więc stała pozycja x ustawiona raz
     * w edytorze mogła się "urywać" na serwerach z dłuższymi nazwami niż ten,
     * na którym pozycję ustawialiśmy.
     */
    private int clampX(int x, int displayWidth) {
        int screenWidth = ResolutionHelper.getWidth();
        float scaledWidth = displayWidth * getSize();
        int maxX = (int) (screenWidth - scaledWidth);
        if (x > maxX) {
            x = Math.max(0, maxX);
            setX(x);
        }
        return x;
    }

    private void drawScoreboard(ScoreObjective objective, boolean background, boolean numbers) {
        Scoreboard scoreboard = objective.getScoreboard();
        Collection<Score> collection = scoreboard.getSortedScores(objective);
        List<Score> list = collection.stream().filter(score -> score.getPlayerName() != null &&
                !score.getPlayerName().startsWith("#")).collect(Collectors.toList());

        if (list.size() > 15) {
            collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
        } else {
            collection = list;
        }

        int displayText = Melon.INSTANCE.mc.fontRendererObj.getStringWidth(objective.getDisplayName()) + 2;

        for (Score score : collection) {
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
            String text = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName()) + ": " + EnumChatFormatting.RED + score.getScorePoints();
            displayText = Math.max(displayText, Melon.INSTANCE.mc.fontRendererObj.getStringWidth(text));
        }

        int y = getY();
        int x = clampX(getX(), displayText + 4);

        int textHeight = Melon.INSTANCE.mc.fontRendererObj.FONT_HEIGHT;

        int index = collection.size() - 1;
        for (Score score1 : collection) {
            ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(score1.getPlayerName());
            String mainText = ScorePlayerTeam.formatPlayerName(scorePlayerTeam, score1.getPlayerName());
            String redNumbers = EnumChatFormatting.RED + "" + score1.getScorePoints();
            int calculatedY = y + index * textHeight;

            if (index == 0) {
                String topText = objective.getDisplayName();
                if (background) {
                    Helper2D.drawRectangle(x, calculatedY, displayText + 4, textHeight, 1610612736);
                    Helper2D.drawRectangle(x, calculatedY + textHeight, displayText + 4, 1, 1342177280);
                }
                Melon.INSTANCE.mc.fontRendererObj.drawString(topText, x + 2 + displayText / 2 - Melon.INSTANCE.mc.fontRendererObj.getStringWidth(topText) / 2, calculatedY + 1, -1);
            }

            if (background) {
                Helper2D.drawRectangle(x, calculatedY + textHeight + 1, displayText + 4, textHeight, 1342177280);
            }
            Melon.INSTANCE.mc.fontRendererObj.drawString(mainText, x + 2, calculatedY + textHeight + 1, -1);
            if (!numbers) {
                Melon.INSTANCE.mc.fontRendererObj.drawString(redNumbers, x + 4 + displayText - Melon.INSTANCE.mc.fontRendererObj.getStringWidth(redNumbers), calculatedY + textHeight + 1, -1);
            }

            index--;
        }

        setW(displayText + 4);
        setH((collection.size() + 1) * textHeight);
    }

    private void drawScoreboardPlaceHolder(boolean background, boolean numbers) {
        String objective = "Scoreboard";
        int displayText = Melon.INSTANCE.mc.fontRendererObj.getStringWidth(objective) + 2;

        String[] names = {"Steve", "Alex", "Zuri", "Sunny", "Noor", "Makena", "Kai", "Efe", "Ari"};
        int collectionSize = names.length;

        for (int i = 0; i < collectionSize; i++) {
            String text = names[i] + ": " + EnumChatFormatting.RED + i;
            displayText = Math.max(displayText, Melon.INSTANCE.mc.fontRendererObj.getStringWidth(text));
        }

        int y = getY();
        int x = clampX(getX(), displayText + 4);

        int textHeight = Melon.INSTANCE.mc.fontRendererObj.FONT_HEIGHT;

        int index = collectionSize - 1;
        for (int i = 0; i < collectionSize; i++) {
            String mainText = names[i];
            String redNumbers = EnumChatFormatting.RED + "" + i;
            int calculatedY = y + index * textHeight;

            if (index == 0) {
                if (background) {
                    Helper2D.drawRectangle(x, calculatedY, displayText + 4, textHeight, 1610612736);
                    Helper2D.drawRectangle(x, calculatedY + textHeight, displayText + 4, 1, 1342177280);
                }
                Melon.INSTANCE.mc.fontRendererObj.drawString(objective, x + 2 + displayText / 2 - Melon.INSTANCE.mc.fontRendererObj.getStringWidth(objective) / 2, calculatedY + 1, -1);
            }

            if (background) {
                Helper2D.drawRectangle(x, calculatedY + textHeight + 1, displayText + 4, textHeight, 1342177280);
            }
            Melon.INSTANCE.mc.fontRendererObj.drawString(mainText, x + 2, calculatedY + textHeight + 1, -1);
            if (!numbers) {
                Melon.INSTANCE.mc.fontRendererObj.drawString(redNumbers, x + 4 + displayText - Melon.INSTANCE.mc.fontRendererObj.getStringWidth(redNumbers), calculatedY + textHeight + 1, -1);
            }

            index--;
        }

        setW(displayText + 4);
        setH((collectionSize + 1) * textHeight);
    }

    private boolean isBackground() {
        return Melon.INSTANCE.settingManager.getSettingByModAndName(getName(), "Background").isCheckToggled();
    }

    private boolean isRemoveNumbers() {
        return Melon.INSTANCE.settingManager.getSettingByModAndName(getName(), "Remove Red Numbers").isCheckToggled();
    }
}


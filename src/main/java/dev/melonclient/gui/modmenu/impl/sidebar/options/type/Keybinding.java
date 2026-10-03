package dev.melonclient.gui.modmenu.impl.sidebar.options.type;

import dev.melonclient.Melon;
import dev.melonclient.feature.option.Option;
import dev.melonclient.gui.Style;
import dev.melonclient.gui.modmenu.impl.Panel;
import dev.melonclient.gui.modmenu.impl.sidebar.options.Options;
import dev.melonclient.helpers.MathHelper;
import dev.melonclient.helpers.render.Helper2D;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class Keybinding extends Options {

    private boolean active;

    public Keybinding(Option option, Panel panel, int y) {
        super(option, panel, y);
    }

    /**
     * Renders the current keybinding and background,
     * Keybinding Setting
     *
     * @param mouseX The current X position of the mouse
     * @param mouseY The current Y position of the mouse
     */

    @Override
    public void renderOption(int mouseX, int mouseY) {
        boolean roundedCorners = Melon.INSTANCE.optionManager.getOptionByName("Rounded Corners").isCheckToggled();
        int color = Melon.INSTANCE.optionManager.getOptionByName("Color").getColor().getRGB();

        Melon.INSTANCE.fontHelper.size30.drawString(
                option.getName(),
                panel.getX() + 20,
                panel.getY() + panel.getH() + 6 + getY(),
                color
        );

        Helper2D.drawRoundedRectangle(
                panel.getX() + panel.getW() - 90,
                panel.getY() + panel.getH() + 2 + getY(),
                70, 20, 2,
                Style.getColor(option.isCheckToggled() ? 80 : 50).getRGB(),
                roundedCorners ? 0 : -1
        );
        Melon.INSTANCE.fontHelper.size20.drawString(
                active ? "?" : Keyboard.getKeyName(option.getKey()),
                panel.getX() + panel.getW() - 55 -
                        Melon.INSTANCE.fontHelper.size20.getStringWidth(active ? "?" : Keyboard.getKeyName(option.getKey())) / 2f,
                panel.getY() + panel.getH() + 8 + getY(),
                color
        );
    }

    /**
     * Changes the active variable if the background of the keybinding is pressed
     *
     * @param mouseX      The current X position of the mouse
     * @param mouseY      The current Y position of the mouse
     * @param mouseButton The current mouse button which is pressed
     */

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (MathHelper.withinBox(panel.getX() + panel.getW() - 140,
                panel.getY() + panel.getH() + 2 + getY(),
                120, 21, mouseX, mouseY)
        ) {
            active = !active;
            if(active) {
                option.setKey(Keyboard.KEY_NONE);
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    /**
     * Checks and sets the button which is pressed if it is active
     */

    @SubscribeEvent
    public void onKey(TickEvent.ClientTickEvent e) {
        int key = Keyboard.getEventKey();
        if (active) {
            option.setKey(key);
            if (Keyboard.isKeyDown(key)) {
                active = false;
            }
        }
    }
}


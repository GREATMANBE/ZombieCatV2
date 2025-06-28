package zombiecat.client.clickgui.components;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.opengl.GL11;

import zombiecat.client.clickgui.Component;
import zombiecat.client.module.Module;
import zombiecat.client.module.modules.client.GuiModule;
import zombiecat.client.module.setting.Setting;

public class ModuleComponent implements Component {
    private final int c1 = new Color(0, 85, 255).getRGB();
    private final int c2 = new Color(154, 2, 255).getRGB();
    private final int c3 = new Color(175, 143, 233).getRGB();

    protected final Module mod;
    protected final CategoryComponent category;

    protected int o;           // offset from top of category
    protected boolean po = false; // whether this module is expanded

    private final List<Component> settingComponents = new ArrayList<>();

    public ModuleComponent(Module mod, CategoryComponent category, int o) {
        this.mod = mod;
        this.category = category;
        this.o = o;

        for (Setting setting : mod.getSettings()) {
            Component component = setting.createComponent(this);
            if (component != null) {
                settingComponents.add(component);
            }
        }

        // Always add a keybind setting at the end
        settingComponents.add(new BindComponent(this, o));
    }

    @Override
    public void draw() {
        int x = category.getX();
        int y = category.getY() + o;
        int width = category.getWidth();

        // Draw module background
        v((float) x, (float) y, (float) (x + width), (float) (y + 15),
            mod.isOn() ? c2 : -12829381,
            mod.isOn() ? c2 : -12302777
        );

        GL11.glPushMatrix();
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        int textColor;
        switch ((int) GuiModule.guiTheme.getValue()) {
            case 3:
                textColor = mod.isOn() ? c1 : (mod.canBeEnabled() ? Color.LIGHT_GRAY.getRGB() : new Color(102, 102, 102).getRGB());
                break;
            case 4:
                textColor = mod.isOn() ? c3 : (mod.canBeEnabled() ? Color.LIGHT_GRAY.getRGB() : new Color(102, 102, 102).getRGB());
                break;
            default:
                textColor = mod.canBeEnabled() ? Color.LIGHT_GRAY.getRGB() : new Color(102, 102, 102).getRGB();
        }

        String name = mod.getName();
        fr.drawStringWithShadow(name, x + width / 2 - fr.getStringWidth(name) / 2, y + 4, textColor);
        GL11.glPopMatrix();

        if (po) {
            int yOffset = y + 16;
            for (Component c : settingComponents) {
                c.setComponentStartAt(yOffset);
                c.draw();
                yOffset += c.getHeight();
            }
        }
    }

    @Override
    public void update(int mouseX, int mouseY) {
        if (po) {
            for (Component c : settingComponents) {
                c.update(mouseX, mouseY);
            }
        }
    }

    @Override
    public void mouseDown(int x, int y, int button) {
        if (isHovered(x, y)) {
            if (button == 0 && mod.canBeEnabled()) {
                mod.toggle();
            } else if (button == 1) {
                po = !po;
                int mouseX = org.lwjgl.input.Mouse.getX();
                int mouseY = Minecraft.getMinecraft().displayHeight - org.lwjgl.input.Mouse.getY() - 1;
                category.r3nd3r(mouseX, mouseY);
            }
        }

        if (po) {
            for (Component c : settingComponents) {
                c.mouseDown(x, y, button);
            }
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        for (Component c : settingComponents) {
            c.mouseReleased(x, y, button);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        for (Component c : settingComponents) {
            c.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void setComponentStartAt(int y) {
        this.o = y;
    }

    @Override
    public int getHeight() {
        int total = 16; // base height
        if (po) {
            for (Component c : settingComponents) {
                total += c.getHeight();
            }
        }
        return total;
    }

    private boolean isHovered(int x, int y) {
        return x > category.getX()
            && x < category.getX() + category.getWidth()
            && y > category.getY() + o
            && y < category.getY() + o + 16;
    }

    // OpenGL Helpers

    public static void e() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
    }

    public static void f() {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_FASTEST);
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_FASTEST);
        GL11.glEdgeFlag(true);
    }

    public static void g(int h) {
        float a = (float) (h >> 24 & 0xFF) / 255.0F;
        float r = (float) (h >> 16 & 0xFF) / 255.0F;
        float g = (float) (h >> 8 & 0xFF) / 255.0F;
        float b = (float) (h & 0xFF) / 255.0F;
        GL11.glColor4f(r, g, b, a);
    }

    public static void v(float x, float y, float x1, float y1, int t, int b) {
        e();
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_QUADS);
        g(t); GL11.glVertex2f(x, y1);
        GL11.glVertex2f(x1, y1);
        g(b); GL11.glVertex2f(x1, y);
        GL11.glVertex2f(x, y);
        GL11.glEnd();
        GL11.glShadeModel(GL11.GL_FLAT);
        f();
    }
}

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
    protected int offsetY;
    protected boolean expanded = false;

    private final List<Component> settingComponents = new ArrayList<>();

    public ModuleComponent(Module mod, CategoryComponent category, int offsetY) {
        this.mod = mod;
        this.category = category;
        this.offsetY = offsetY;

        for (Setting setting : mod.getSettings()) {
            Component component = setting.createComponent(this);
            if (component != null) {
                settingComponents.add(component);
            }
        }

        settingComponents.add(new BindComponent(this, offsetY));
    }

    @Override
    public void draw() {
        int x = category.getX();
        int y = category.getY() + offsetY;
        int width = category.getWidth();

        // Background gradient
        v((float) x, (float) y, (float) (x + width), (float) (y + 15),
            mod.isOn() ? c2 : -12829381,
            mod.isOn() ? c2 : -12302777
        );

        GL11.glPushMatrix();

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        int textColor;
        switch ((int) GuiModule.guiTheme.getValue()) {
            case 3:
                textColor = mod.isOn() ? c1 : mod.canBeEnabled() ? Color.LIGHT_GRAY.getRGB() : new Color(102, 102, 102).getRGB();
                break;
            case 4:
                textColor = mod.isOn() ? c3 : mod.canBeEnabled() ? Color.LIGHT_GRAY.getRGB() : new Color(102, 102, 102).getRGB();
                break;
            default:
                textColor = mod.canBeEnabled() ? Color.LIGHT_GRAY.getRGB() : new Color(102, 102, 102).getRGB();
                break;
        }

        String name = mod.getName();
        fr.drawStringWithShadow(name, x + width / 2 - fr.getStringWidth(name) / 2, y + 4, textColor);
        GL11.glPopMatrix();

        if (expanded) {
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
        if (expanded) {
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
                expanded = !expanded;
                category.r3nd3r();
            }
        }

        if (expanded) {
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
        this.offsetY = y;
    }

    @Override
    public int getHeight() {
        int total = 16;
        if (expanded) {
            for (Component c : settingComponents) {
                total += c.getHeight();
            }
        }
        return total;
    }

    private boolean isHovered(int x, int y) {
        return x > category.getX()
            && x < category.getX() + category.getWidth()
            && y > category.getY() + offsetY
            && y < category.getY() + offsetY + 16;
    }

    // OpenGL helpers
    public static void e() {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
    }

    public static void f() {
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
        GL11.glEdgeFlag(true);
    }

    public static void g(int h) {
        float a = 0.0F;
        float r = 0.0F;
        float g = 0.0F;
        float b = 0.0F;
        if (GuiModule.guiTheme.getValue() == 1.0) {
            a = (float) (h >> 14 & 0xFF) / 255.0F;
            r = (float) (h >> 5 & 0xFF) / 255.0F;
            g = (float) (h >> 5 & 0xFF) / 2155.0F;
            b = (float) (h & 0xFF);
        } else if (GuiModule.guiTheme.getValue() == 2.0) {
            a = (float) (h >> 14 & 0xFF) / 255.0F;
            r = (float) (h >> 5 & 0xFF) / 2155.0F;
            g = (float) (h >> 5 & 0xFF) / 255.0F;
            b = (float) (h & 0xFF);
        }

        GL11.glColor4f(r, g, b, a);
    }

    public static void v(float x, float y, float x1, float y1, int t, int b) {
        e();
        GL11.glShadeModel(7425);
        GL11.glBegin(7);
        g(t);
        GL11.glVertex2f(x, y1);
        GL11.glVertex2f(x1, y1);
        g(b);
        GL11.glVertex2f(x1, y);
        GL11.glVertex2f(x, y);
        GL11.glEnd();
        GL11.glShadeModel(7424);
        f();
    }
}

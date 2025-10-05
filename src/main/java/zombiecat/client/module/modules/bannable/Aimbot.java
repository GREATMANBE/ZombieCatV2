package zombiecat.client.module.modules.bannable;

import java.io.File;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import zombiecat.client.module.Module;
import zombiecat.client.module.setting.Setting;
import zombiecat.client.module.setting.impl.BooleanSetting;
import zombiecat.client.module.setting.impl.SliderSetting;

public class Aimbot extends Module {
  public static BooleanSetting onlyFire;
  
  public static BooleanSetting pup;
  
  public static BooleanSetting pierce;
  
  public static BooleanSetting mobPriority;
  
  public static BooleanSetting skelePriority;
  
  public static BooleanSetting insta;
  
  public static BooleanSetting autoInsta;
  
  public static BooleanSetting autoFlick;
  
  public static BooleanSetting preFlick;
  
  public static BooleanSetting creeper;
  
  public static BooleanSetting bbr1;
  
  public static SliderSetting predict;
  
  public static SliderSetting yPredict;
  
  public static SliderSetting crits;
  
  public static SliderSetting vcrits;
  
  public static SliderSetting flick;
  
  public static BooleanSetting close;
  
  private static final double HEAD_FRAC_BASE = 0.9D;
  
  private static final double HEAD_FRAC_SLOPE = 0.2D;
  
  private static final double CRITS_MIN = -0.2D;
  
  private static final double CRITS_MAX = 0.5D;
  
  private static final double CRITS_EXT_MIN = -2.0D;
  
  private static final double CRITS_EXT_MAX = 2.0D;
  
  private static final double HEAD_FRAC_MIN = 0.05D;
  
  private static final double VCRITS_FLOOR = 0.5D;
  
  private static final double Y_EPS = 0.01D;
  
  private static volatile double USER_HEAD_FRAC_MAX = 0.98D;
  
  private static final double USER_HEAD_FRAC_MAX_MIN = 0.01D;
  
  private static final double USER_HEAD_FRAC_MAX_MAX = 2.0D;
  
  private static final String CFG_CAT = "aim";
  
  private static final String CFG_KEY_CLAMP = "headFracMax";
  
  private static final String CFG_COMMENT_CLAMP = "Max head aim as fraction of mob height (1.00 = 100%). Range 0.01..2.00.";
  
  private static final String CFG_KEY_PREFLICK_HP = "preFlickHp";
  
  private static final String CFG_COMMENT_PREFLICK_HP = "HP threshold for PreFlick lock-on.";
  
  private static final String CFG_KEY_PREFLICK_SPEED = "preFlickSpeedSeconds";
  
  private static final String CFG_COMMENT_PREFLICK_SPEED = "Lock-on duration in seconds for PreFlick.";
  
  private static Configuration cfg;
  
  private static final long CHAT_INSTA_AUTO_DISABLE_MS = 12000L;
  
  private static final int FLICK_COOLDOWN_TICKS = 2;
  
  private static final long BBR1_DELAY_MS = 18000L;
  
  private static final long BBR1_WINDOW_MS = 10000L;
  
  private Integer currentTargetId = null;
  
  private int currentDwellLeft = 0;
  
  private final Map<Integer, Integer> cooldownTicks = new HashMap<>();
  
  private static volatile double preFlickHp = 6.0D;
  
  private static volatile double preFlickSpeedSeconds = 0.0D;
  
  private long preFlickLockExpiresAt = -1L;
  
  private final Map<Integer, Long> postFlickCooldowns = new ConcurrentHashMap<>();
  
  private static volatile long instaAutoDisableAtMs = -1L;
  
  private static volatile long bbr1EnableAtMs = -1L;
  
  private static volatile long bbr1DisableAtMs = -1L;
  
  private static boolean CHAT_HOOK_REGISTERED = false;
  
  private static boolean COMMAND_REGISTERED = false;
  
  private static volatile boolean critsOverrideActive = false;
  
  private static volatile double critsOverrideValue = 0.0D;
  
  private static volatile boolean predictOverrideActive = false;
  
  private static volatile double predictOverrideValue = 0.0D;
  
  private static volatile boolean yPredictOverrideActive = false;
  
  private static volatile double yPredictOverrideValue = 0.0D;
  
  private static volatile boolean vcritsOverrideActive = false;
  
  private static volatile double vcritsOverrideValue = 0.0D;
  
  private static double lastGuiCritsValue = 0.0D;
  
  private static double lastGuiPredictValue = 0.0D;
  
  private static double lastGuiYPredictValue = 0.0D;
  
  private static double lastGuiVCritsValue = 0.0D;
  
  private static int guiCritsLockTicks = 0;
  
  private static int guiPredictLockTicks = 0;
  
  private static int guiYPredictLockTicks = 0;
  
  private static int guiVCritsLockTicks = 0;
  
  private static final double Y_RES = 0.05D;
  
  private static final double R1 = 0.35D;
  
  private static final double R2 = 0.7D;
  
  private static final double R3 = 0.95D;
  
  private static volatile boolean lowSlimeMode = true;
  
  private static volatile boolean lowGiantMode = false;
  
  private enum PriorityMode {
    DEFAULT, GIANT, GHAST, SENTINEL, SLIME, BLAZE;
  }
  
  private static volatile PriorityMode priorityMode = PriorityMode.DEFAULT;
  
  private static volatile long priorityExpireAtMs = -1L;
  
  private static volatile long mobPrioExpireAtMs = -1L;
  
  private static volatile long skelePrioExpireAtMs = -1L;
  
  private static int mobGuiLockTicks = 0;
  
  private static int skeleGuiLockTicks = 0;
  
  private static boolean lastMobGuiVal = false;
  
  private static boolean lastSkeleGuiVal = false;
  
  public Aimbot() {
    super("Aimbot", Module.ModuleCategory.bannable);
    registerSetting((Setting)(onlyFire = new BooleanSetting("OnlyFire", true)));
    registerSetting((Setting)(pup = new BooleanSetting("Pup", false)));
    registerSetting((Setting)(pierce = new BooleanSetting("Pierce", false)));
    registerSetting((Setting)(mobPriority = new BooleanSetting("MobPriority", false)));
    registerSetting((Setting)(skelePriority = new BooleanSetting("SkelePriority", false)));
    registerSetting((Setting)(insta = new BooleanSetting("Insta", false)));
    registerSetting((Setting)(autoInsta = new BooleanSetting("AutoInsta", true)));
    registerSetting((Setting)(autoFlick = new BooleanSetting("AutoFlick", true)));
    registerSetting((Setting)(preFlick = new BooleanSetting("PreFlick", false)));
    registerSetting((Setting)(creeper = new BooleanSetting("Creeper", false)));
    registerSetting((Setting)(close = new BooleanSetting("Close", false)));
    registerSetting((Setting)(bbr1 = new BooleanSetting("BBR1", false)));
    registerSetting((Setting)(predict = new SliderSetting("Predict", 4.0D, 0.0D, 10.0D, 0.1D)));
    registerSetting((Setting)(yPredict = new SliderSetting("YPredict", 4.0D, 0.0D, 10.0D, 0.1D)));
    registerSetting((Setting)(crits = new SliderSetting("Crits", 0.0D, -0.2D, 0.5D, 0.01D)));
    registerSetting((Setting)(vcrits = new SliderSetting("VCrits", 0.0D, 0.0D, 1.0D, 0.01D)));
    registerSetting((Setting)(flick = new SliderSetting("Flick", 0.0D, 0.0D, 10.0D, 1.0D)));
    lastMobGuiVal = mobPriority.getValue();
    lastSkeleGuiVal = skelePriority.getValue();
    lastGuiCritsValue = crits.getValue();
    lastGuiPredictValue = predict.getValue();
    lastGuiYPredictValue = yPredict.getValue();
    lastGuiVCritsValue = vcrits.getValue();
    loadConfig();
    if (!CHAT_HOOK_REGISTERED) {
      MinecraftForge.EVENT_BUS.register(new GlobalChatHookIncoming());
      CHAT_HOOK_REGISTERED = true;
    } 
    if (!COMMAND_REGISTERED) {
      ClientCommandHandler.instance.func_71560_a((ICommand)new ClampCommand());
      ClientCommandHandler.instance.func_71560_a((ICommand)new CritsCommand());
      ClientCommandHandler.instance.func_71560_a((ICommand)new PredictCommand());
      ClientCommandHandler.instance.func_71560_a((ICommand)new YPredictCommand());
      ClientCommandHandler.instance.func_71560_a((ICommand)new VCritsCommand());
      ClientCommandHandler.instance.func_71560_a((ICommand)new FlickCommand());
      ClientCommandHandler.instance.func_71560_a((ICommand)new LowSlimeCommand());
      ClientCommandHandler.instance.func_71560_a((ICommand)new HighSlimeCommand());
      ClientCommandHandler.instance.func_71560_a((ICommand)new LowGiantCommand());
      ClientCommandHandler.instance.func_71560_a((ICommand)new HighGiantCommand());
      ClientCommandHandler.instance.func_71560_a((ICommand)new PriorityCommand());
      ClientCommandHandler.instance.func_71560_a((ICommand)new PreFlickHpCommand());
      ClientCommandHandler.instance.func_71560_a((ICommand)new PreFlickSpeedCommand());
      COMMAND_REGISTERED = true;
    } 
  }
  
  private static void loadConfig() {
    try {
      File cfgDir = new File(mc.field_71412_D, "config");
      if (!cfgDir.exists())
        cfgDir.mkdirs(); 
      File file = new File(cfgDir, "zombiecat_aimbot.cfg");
      cfg = new Configuration(file);
      cfg.load();
      float def = 0.98F;
      float val = cfg.getFloat("headFracMax", "aim", def, 0.01F, 2.0F, "Max head aim as fraction of mob height (1.00 = 100%). Range 0.01..2.00.");
      USER_HEAD_FRAC_MAX = clamp(val, 0.01F, 2.0F);
      preFlickHp = cfg.getFloat("preFlickHp", "aim", 6.0F, 0.1F, 1000.0F, "HP threshold for PreFlick lock-on.");
      preFlickSpeedSeconds = cfg.getFloat("preFlickSpeedSeconds", "aim", 0.0F, 0.0F, 60.0F, "Lock-on duration in seconds for PreFlick.");
      if (cfg.hasChanged())
        cfg.save(); 
    } catch (Throwable throwable) {}
  }
  
  private static void saveConfig() {
    try {
      if (cfg == null)
        return; 
      cfg.get("aim", "headFracMax", 0.98D, "Max head aim as fraction of mob height (1.00 = 100%). Range 0.01..2.00.").set(USER_HEAD_FRAC_MAX);
      cfg.get("aim", "preFlickHp", 6.0D, "HP threshold for PreFlick lock-on.").set(preFlickHp);
      cfg.get("aim", "preFlickSpeedSeconds", 0.0D, "Lock-on duration in seconds for PreFlick.").set(preFlickSpeedSeconds);
      if (cfg.hasChanged())
        cfg.save(); 
    } catch (Throwable throwable) {}
  }
  
  private static class GlobalChatHookIncoming {
    private GlobalChatHookIncoming() {}
    
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent e) {
      if (e == null || e.message == null)
        return; 
      String norm = Aimbot.normalizeChat(e.message.func_150260_c());
      if (Aimbot.autoInsta != null && Aimbot.autoInsta.getValue() && (
        norm.contains("activated insta kill") || norm.contains("insta kill activated"))) {
        Aimbot.trySetBoolean(Aimbot.insta, true);
        Aimbot.instaAutoDisableAtMs = System.currentTimeMillis() + 12000L;
        Aimbot.sendClientMsg("Insta Enabled");
      } 
      if (Aimbot.bbr1 != null && Aimbot.bbr1.getValue() && 
        norm.contains("fight with your teammates")) {
        long now = System.currentTimeMillis();
        Aimbot.bbr1EnableAtMs = now + 18000L;
        Aimbot.bbr1DisableAtMs = Aimbot.bbr1EnableAtMs + 10000L;
      } 
    }
    
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
      long now = System.currentTimeMillis();
      if (Aimbot.priorityExpireAtMs > 0L && now >= Aimbot.priorityExpireAtMs) {
        Aimbot.priorityMode = Aimbot.PriorityMode.DEFAULT;
        Aimbot.priorityExpireAtMs = -1L;
        Aimbot.sendClientMsg("Priority expired );
      } 
      if (Aimbot.instaAutoDisableAtMs >= 0L && now >= Aimbot.instaAutoDisableAtMs) {
        Aimbot.trySetBoolean(Aimbot.insta, false);
        Aimbot.instaAutoDisableAtMs = -1L;
        Aimbot.sendClientMsg("Insta Disabled");
      } 
      if (Aimbot.bbr1EnableAtMs > 0L && now >= Aimbot.bbr1EnableAtMs) {
        Aimbot.trySetBoolean(Aimbot.insta, true);
        Aimbot.bbr1EnableAtMs = -1L;
        Aimbot.sendClientMsg("Insta Enabled");
      } 
      if (Aimbot.bbr1DisableAtMs > 0L && now >= Aimbot.bbr1DisableAtMs) {
        Aimbot.trySetBoolean(Aimbot.insta, false);
        Aimbot.bbr1DisableAtMs = -1L;
        Aimbot.sendClientMsg("Insta Disabled");
      } 
      if (Aimbot.mobPriority != null) {
        boolean gv = Aimbot.mobPriority.getValue();
        if (Aimbot.mobGuiLockTicks > 0) {
          Aimbot.mobGuiLockTicks--;
          Aimbot.lastMobGuiVal = gv;
        } else if (gv != Aimbot.lastMobGuiVal) {
          Aimbot.mobPrioExpireAtMs = -1L;
          Aimbot.lastMobGuiVal = gv;
        } 
      } 
      if (Aimbot.skelePriority != null) {
        boolean gv = Aimbot.skelePriority.getValue();
        if (Aimbot.skeleGuiLockTicks > 0) {
          Aimbot.skeleGuiLockTicks--;
          Aimbot.lastSkeleGuiVal = gv;
        } else if (gv != Aimbot.lastSkeleGuiVal) {
          Aimbot.skelePrioExpireAtMs = -1L;
          Aimbot.lastSkeleGuiVal = gv;
        } 
      } 
      if (Aimbot.mobPrioExpireAtMs > 0L && now >= Aimbot.mobPrioExpireAtMs) {
        Aimbot.trySetBoolean(Aimbot.mobPriority, false);
        Aimbot.lastMobGuiVal = Aimbot.mobPriority.getValue();
        Aimbot.mobPrioExpireAtMs = -1L;
        Aimbot.sendClientMsg("expired.");
      } 
      if (Aimbot.skelePrioExpireAtMs > 0L && now >= Aimbot.skelePrioExpireAtMs) {
        Aimbot.trySetBoolean(Aimbot.skelePriority, false);
        Aimbot.lastSkeleGuiVal = Aimbot.skelePriority.getValue();
        Aimbot.skelePrioExpireAtMs = -1L;
        Aimbot.sendClientMsg("expired.");
      } 
    }
  }
  
  private static class ClampCommand extends CommandBase {
    private ClampCommand() {}
    
    public String func_71517_b() {
      return "clamp";
    }
    
    public String func_71518_a(ICommandSender sender) {
      return "/clamp <percent 1..200> | /clamp reset";
    }
    
    public int func_82362_a() {
      return 0;
    }
    
    public boolean func_71519_b(ICommandSender s) {
      return true;
    }
    
    public void func_71515_b(ICommandSender sender, String[] args) {
      if (args.length == 0) {
        Aimbot.sendClientMsg("Clamp is currently + Aimbot.fmtPct3(Aimbot.USER_HEAD_FRAC_MAX * 100.0D) + "%" + Aimbot.critsAtClampText());
        return;
      } 
      if ("reset".equalsIgnoreCase(args[0])) {
        Aimbot.USER_HEAD_FRAC_MAX = 0.98D;
        Aimbot.saveConfig();
        Aimbot.sendClientMsg("Clamp reset to + Aimbot.fmtPct3(98.0D) + "%" + Aimbot.critsAtClampText());
        return;
      } 
      try {
        double pct = Double.parseDouble(args[0]);
        double clampedPct = Aimbot.clamp(pct, 1.0D, 200.0D);
        Aimbot.USER_HEAD_FRAC_MAX = Aimbot.clamp(clampedPct / 100.0D, 0.01D, 2.0D);
        Aimbot.saveConfig();
        Aimbot.sendClientMsg("Clamp set to + Aimbot.fmtPct3(clampedPct) + "%" + Aimbot.critsAtClampText());
      } catch (NumberFormatException ex) {
        Aimbot.sendClientMsg("Invalid number. Usage: + func_71518_a(sender));
      } 
    }
  }
  
  private static class CritsCommand extends CommandBase {
    private CritsCommand() {}
    
    public String func_71517_b() {
      return "crits";
    }
    
    public String func_71518_a(ICommandSender sender) {
      return "/crits <value -2.000..+2.000>";
    }
    
    public int func_82362_a() {
      return 0;
    }
    
    public boolean func_71519_b(ICommandSender s) {
      return true;
    }
    
    public void func_71515_b(ICommandSender sender, String[] args) {
      if (args.length == 0) {
        double cur = Aimbot.getCritsActive();
        Aimbot.sendClientMsg("Crits is + Aimbot.fmt3(cur));
        return;
      } 
      try {
        double v = Aimbot.clamp(Double.parseDouble(args[0]), -2.0D, 2.0D);
        if (v >= -0.2D && v <= 0.5D) {
          Aimbot.setGuiCritsSlider(v);
          Aimbot.critsOverrideActive = false;
          Aimbot.critsOverrideValue = 0.0D;
        } else {
          double nearestGui = (v < -0.2D) ? -0.2D : 0.5D;
          Aimbot.setGuiCritsSlider(nearestGui);
          Aimbot.critsOverrideActive = true;
          Aimbot.critsOverrideValue = v;
        } 
        Aimbot.sendClientMsg("Crits set to + Aimbot.fmt3(v));
      } catch (NumberFormatException ex) {
        Aimbot.sendClientMsg("Invalid number. Usage: + func_71518_a(sender));
      } 
    }
  }
  
  private static class PredictCommand extends CommandBase {
    private PredictCommand() {}
    
    public String func_71517_b() {
      return "predict";
    }
    
    public String func_71518_a(ICommandSender sender) {
      return "/predict <value 0.000..10.000>";
    }
    
    public int func_82362_a() {
      return 0;
    }
    
    public boolean func_71519_b(ICommandSender s) {
      return true;
    }
    
    public void func_71515_b(ICommandSender sender, String[] args) {
      if (Aimbot.predict == null)
        return; 
      if (args.length == 0) {
        Aimbot.sendClientMsg("Predict is + Aimbot.fmt3(Aimbot.getPredictActive()));
        return;
      } 
      try {
        double v = Aimbot.clamp(Double.parseDouble(args[0]), 0.0D, 10.0D);
        Aimbot.trySetSlider(Aimbot.predict, v);
        Aimbot.guiPredictLockTicks = 2;
        Aimbot.lastGuiPredictValue = Aimbot.predict.getValue();
        Aimbot.predictOverrideActive = true;
        Aimbot.predictOverrideValue = v;
        Aimbot.sendClientMsg("Predict set to + Aimbot.fmt3(v));
      } catch (NumberFormatException ex) {
        Aimbot.sendClientMsg("Invalid number. Usage: + func_71518_a(sender));
      } 
    }
  }
  
  private static class YPredictCommand extends CommandBase {
    private YPredictCommand() {}
    
    public String func_71517_b() {
      return "ypredict";
    }
    
    public String func_71518_a(ICommandSender sender) {
      return "/ypredict <value 0.000..10.000>";
    }
    
    public int func_82362_a() {
      return 0;
    }
    
    public boolean func_71519_b(ICommandSender s) {
      return true;
    }
    
    public void func_71515_b(ICommandSender sender, String[] args) {
      if (Aimbot.yPredict == null)
        return; 
      if (args.length == 0) {
        Aimbot.sendClientMsg("YPredict is + Aimbot.fmt3(Aimbot.getYPredictActive()));
        return;
      } 
      try {
        double v = Aimbot.clamp(Double.parseDouble(args[0]), 0.0D, 10.0D);
        Aimbot.trySetSlider(Aimbot.yPredict, v);
        Aimbot.guiYPredictLockTicks = 2;
        Aimbot.lastGuiYPredictValue = Aimbot.yPredict.getValue();
        Aimbot.yPredictOverrideActive = true;
        Aimbot.yPredictOverrideValue = v;
        Aimbot.sendClientMsg("YPredict set to + Aimbot.fmt3(v));
      } catch (NumberFormatException ex) {
        Aimbot.sendClientMsg("Invalid number. Usage: + func_71518_a(sender));
      } 
    }
  }
  
  private static class VCritsCommand extends CommandBase {
    private VCritsCommand() {}
    
    public String func_71517_b() {
      return "vcrits";
    }
    
    public String func_71518_a(ICommandSender sender) {
      return "/vcrits <value 0.000..1.000>";
    }
    
    public int func_82362_a() {
      return 0;
    }
    
    public boolean func_71519_b(ICommandSender s) {
      return true;
    }
    
    public void func_71515_b(ICommandSender sender, String[] args) {
      if (Aimbot.vcrits == null)
        return; 
      if (args.length == 0) {
        Aimbot.sendClientMsg("VCrits is + Aimbot.fmt3(Aimbot.getVCritsActive()));
        return;
      } 
      try {
        double v = Aimbot.clamp(Double.parseDouble(args[0]), 0.0D, 1.0D);
        Aimbot.trySetSlider(Aimbot.vcrits, v);
        Aimbot.guiVCritsLockTicks = 2;
        Aimbot.lastGuiVCritsValue = Aimbot.vcrits.getValue();
        Aimbot.vcritsOverrideActive = true;
        Aimbot.vcritsOverrideValue = v;
        Aimbot.sendClientMsg("VCrits set to + Aimbot.fmt3(v));
      } catch (NumberFormatException ex) {
        Aimbot.sendClientMsg("Invalid number. Usage: + func_71518_a(sender));
      } 
    }
  }
  
  private static class FlickCommand extends CommandBase {
    private FlickCommand() {}
    
    public String func_71517_b() {
      return "flick";
    }
    
    public String func_71518_a(ICommandSender sender) {
      return "/flick <ticks 0..10>";
    }
    
    public int func_82362_a() {
      return 0;
    }
    
    public boolean func_71519_b(ICommandSender s) {
      return true;
    }
    
    public void func_71515_b(ICommandSender sender, String[] args) {
      if (Aimbot.flick == null)
        return; 
      if (args.length == 0) {
        Aimbot.sendClientMsg("Flick is + Math.round(Aimbot.flick.getValue()) + "ticks");
        return;
      } 
      try {
        double raw = Double.parseDouble(args[0]);
        int ticks = (int)Math.round(raw);
        ticks = (int)Aimbot.clamp(ticks, 0.0F, 10.0F);
        Aimbot.trySetSlider(Aimbot.flick, ticks);
        Aimbot.sendClientMsg("Flick set to + Math.round(Aimbot.flick.getValue()) + "ticks");
      } catch (NumberFormatException ex) {
        Aimbot.sendClientMsg("Invalid number. Usage: + func_71518_a(sender));
      } 
    }
  }
  
  private static class PreFlickHpCommand extends CommandBase {
    private PreFlickHpCommand() {}
    
    public String func_71517_b() {
      return "preflickhp";
    }
    
    public List func_71514_a() {
      return Arrays.asList(new String[] { "pfhp" });
    }
    
    public String func_71518_a(ICommandSender sender) {
      return "/preflickhp (alias: /pfhp) <health>";
    }
    
    public int func_82362_a() {
      return 0;
    }
    
    public boolean func_71519_b(ICommandSender s) {
      return true;
    }
    
    public void func_71515_b(ICommandSender sender, String[] args) {
      if (args.length == 0) {
        Aimbot.sendClientMsg("PreFlick HP threshold is + Aimbot.fmt3(Aimbot.preFlickHp));
        return;
      } 
      try {
        double hp = Double.parseDouble(args[0]);
        Aimbot.preFlickHp = Math.max(0.1D, hp);
        Aimbot.saveConfig();
        Aimbot.sendClientMsg("PreFlick HP set to + Aimbot.fmt3(Aimbot.preFlickHp));
      } catch (NumberFormatException ex) {
        Aimbot.sendClientMsg("Invalid number. Usage: + func_71518_a(sender));
      } 
    }
  }
  
  private static class PreFlickSpeedCommand extends CommandBase {
    private PreFlickSpeedCommand() {}
    
    public String func_71517_b() {
      return "preflickspeed";
    }
    
    public List func_71514_a() {
      return Arrays.asList(new String[] { "pfs" });
    }
    
    public String func_71518_a(ICommandSender sender) {
      return "/preflickspeed (alias: /pfs) <seconds>";
    }
    
    public int func_82362_a() {
      return 0;
    }
    
    public boolean func_71519_b(ICommandSender s) {
      return true;
    }
    
    public void func_71515_b(ICommandSender sender, String[] args) {
      if (args.length == 0) {
        Aimbot.sendClientMsg("PreFlick speed (lock duration) is + Aimbot.fmt3(Aimbot.preFlickSpeedSeconds) + "s");
        return;
      } 
      try {
        double speed = Double.parseDouble(args[0]);
        Aimbot.preFlickSpeedSeconds = Math.max(0.0D, speed);
        Aimbot.saveConfig();
        Aimbot.sendClientMsg("PreFlick speed set to + Aimbot.fmt3(Aimbot.preFlickSpeedSeconds) + "s");
      } catch (NumberFormatException ex) {
        Aimbot.sendClientMsg("Invalid number. Usage: + func_71518_a(sender));
      } 
    }
  }
  
  private static class LowSlimeCommand extends CommandBase {
    private LowSlimeCommand() {}
    
    public String func_71517_b() {
      return "lowslime";
    }
    
    public String func_71518_a(ICommandSender sender) {
      return "/lowslime";
    }
    
    public int func_82362_a() {
      return 0;
    }
    
    public boolean func_71519_b(ICommandSender s) {
      return true;
    }
    
    public void func_71515_b(ICommandSender sender, String[] args) {
      Aimbot.lowSlimeMode = true;
      Aimbot.sendClientMsg("Slime/Magma aim: );
    }
  }
  
  private static class HighSlimeCommand extends CommandBase {
    private HighSlimeCommand() {}
    
    public String func_71517_b() {
      return "highslime";
    }
    
    public String func_71518_a(ICommandSender sender) {
      return "/highslime";
    }
    
    public int func_82362_a() {
      return 0;
    }
    
    public boolean func_71519_b(ICommandSender s) {
      return true;
    }
    
    public void func_71515_b(ICommandSender sender, String[] args) {
      Aimbot.lowSlimeMode = false;
      Aimbot.sendClientMsg("Slime/Magma aim: );
    }
  }
  
  private static class LowGiantCommand extends CommandBase {
    private LowGiantCommand() {}
    
    public String func_71517_b() {
      return "lowgiant";
    }
    
    public String func_71518_a(ICommandSender sender) {
      return "/lowgiant";
    }
    
    public int func_82362_a() {
      return 0;
    }
    
    public boolean func_71519_b(ICommandSender s) {
      return true;
    }
    
    public void func_71515_b(ICommandSender sender, String[] args) {
      Aimbot.lowGiantMode = true;
      Aimbot.sendClientMsg("Giant aim: (fixed at base+3.0 blocks).");
    }
  }
  
  private static class HighGiantCommand extends CommandBase {
    private HighGiantCommand() {}
    
    public String func_71517_b() {
      return "highgiant";
    }
    
    public String func_71518_a(ICommandSender sender) {
      return "/highgiant";
    }
    
    public int func_82362_a() {
      return 0;
    }
    
    public boolean func_71519_b(ICommandSender s) {
      return true;
    }
    
    public void func_71515_b(ICommandSender sender, String[] args) {
      Aimbot.lowGiantMode = false;
      Aimbot.sendClientMsg("Giant aim: (normal adaptive aim).");
    }
  }
  
  private static class PriorityCommand extends CommandBase {
    private PriorityCommand() {}
    
    private static final String[] PRIMARY_OPTIONS = new String[] { "giant", "ghast", "sentinel", "slime", "blaze", "default", "skeleton", "mob", "skele" };
    
    private static final String[] DURATION_SUGGESTIONS = new String[] { "5", "10", "15", "20", "30", "60", "120", "off" };
    
    public String func_71517_b() {
      return "priority";
    }
    
    public List func_71514_a() {
      return Arrays.asList(new String[] { "prio" });
    }
    
    public String func_71518_a(ICommandSender sender) {
      return "/priority (alias: /prio) [giant|ghast|sentinel|slime|blaze|default|skeleton|skele|mob] [seconds|off]";
    }
    
    public int func_82362_a() {
      return 0;
    }
    
    public boolean func_71519_b(ICommandSender s) {
      return true;
    }
    
    public List func_180525_a(ICommandSender sender, String[] args, BlockPos pos) {
      if (args.length == 1)
        return func_71530_a(args, PRIMARY_OPTIONS); 
      if (args.length == 2) {
        String first = args[0].toLowerCase(Locale.ROOT);
        if (first.equals("giant") || first.equals("ghast") || first.equals("sentinel") || first
          .equals("slime") || first.equals("default") || first.equals("sentenial") || first.equals("sen") || first
          .equals("skeleton") || first.equals("skele") || first.equals("mob"))
          return func_71530_a(args, DURATION_SUGGESTIONS); 
      } 
      return Collections.emptyList();
    }
    
    public void func_71515_b(ICommandSender sender, String[] args) {
      Aimbot.PriorityMode newMode;
      if (args.length == 0) {
        String modeStr = Aimbot.priorityMode.name().toLowerCase(Locale.ROOT);
        String tail = "";
        if (Aimbot.priorityExpireAtMs > 0L) {
          long left = Math.max(0L, Aimbot.priorityExpireAtMs - System.currentTimeMillis());
          tail = " + (left / 1000L) + "s left)";
        } 
        String mobStr = (Aimbot.mobPriority != null && Aimbot.mobPriority.getValue()) ? ": ";
        String skeleStr = (Aimbot.skelePriority != null && Aimbot.skelePriority.getValue()) ? ": ";
        String mobTail = (Aimbot.mobPrioExpireAtMs > 0L) ? (" + ((Aimbot.mobPrioExpireAtMs - System.currentTimeMillis()) / 1000L) + "s left)") : "";
        String skeTail = (Aimbot.skelePrioExpireAtMs > 0L) ? (" + ((Aimbot.skelePrioExpireAtMs - System.currentTimeMillis()) / 1000L) + "s left)") : "";
        Aimbot.sendClientMsg("Priority: + modeStr + tail + "| MobPriority: " + mobStr + mobTail + "| SkelePriority: " + skeleStr + skeTail);
        return;
      } 
      String which = args[0].toLowerCase(Locale.ROOT);
      if (which.equals("skeleton") || which.equals("skele") || which.equals("mob")) {
        boolean isSkele = !which.equals("mob");
        BooleanSetting target = isSkele ? Aimbot.skelePriority : Aimbot.mobPriority;
        if (target == null) {
          Aimbot.sendClientMsg("GUI setting not available.");
          return;
        } 
        boolean turnOn = true;
        Long long_ = null;
        if (args.length >= 2) {
          String s = args[1].toLowerCase(Locale.ROOT);
          if (s.equals("off") || s.equals("0")) {
            turnOn = false;
          } else {
            try {
              int secs = Integer.parseInt(s);
              secs = (int)Aimbot.clamp(secs, 1.0F, 3600.0F);
              long_ = Long.valueOf(secs * 1000L);
              turnOn = true;
            } catch (NumberFormatException ex) {
              Aimbot.sendClientMsg("Invalid seconds. Usage: + func_71518_a(sender));
              return;
            } 
          } 
        } 
        Aimbot.trySetBoolean(target, turnOn);
        long now = System.currentTimeMillis();
        if (isSkele) {
          Aimbot.skelePrioExpireAtMs = (turnOn && long_ != null) ? (now + long_.longValue()) : -1L;
          Aimbot.sendClientMsg(turnOn ? ("enabled" + ((long_ != null) ? (" for + (long_
              .longValue() / 1000L) + "s) : ".")) : "disabled.");
          Aimbot.skeleGuiLockTicks = 2;
          Aimbot.lastSkeleGuiVal = target.getValue();
        } else {
          Aimbot.mobPrioExpireAtMs = (turnOn && long_ != null) ? (now + long_.longValue()) : -1L;
          Aimbot.sendClientMsg(turnOn ? ("enabled" + ((long_ != null) ? (" for + (long_
              .longValue() / 1000L) + "s) : ".")) : "disabled.");
          Aimbot.mobGuiLockTicks = 2;
          Aimbot.lastMobGuiVal = target.getValue();
        } 
        return;
      } 
      if (which.equals("giant")) {
        newMode = Aimbot.PriorityMode.GIANT;
      } else if (which.equals("ghast")) {
        newMode = Aimbot.PriorityMode.GHAST;
      } else if (which.equals("sentinel") || which.equals("sentenial") || which.equals("sen")) {
        newMode = Aimbot.PriorityMode.SENTINEL;
      } else if (which.equals("slime")) {
        newMode = Aimbot.PriorityMode.SLIME;
      } else if (which.equals("blaze")) {
        newMode = Aimbot.PriorityMode.BLAZE;
      } else if (which.equals("default")) {
        newMode = Aimbot.PriorityMode.DEFAULT;
      } else {
        Aimbot.sendClientMsg("Unknown priority. Usage: + func_71518_a(sender));
        return;
      } 
      long durationMs = -1L;
      if (args.length >= 2)
        try {
          int secs = Integer.parseInt(args[1]);
          secs = (int)Aimbot.clamp(secs, 1.0F, 3600.0F);
          durationMs = secs * 1000L;
        } catch (NumberFormatException ex) {
          Aimbot.sendClientMsg("Invalid seconds. Usage: + func_71518_a(sender));
          return;
        }  
      Aimbot.priorityMode = newMode;
      if (durationMs > 0L) {
        Aimbot.priorityExpireAtMs = System.currentTimeMillis() + durationMs;
        Aimbot.sendClientMsg("Priority set to + which + "for + (durationMs / 1000L) + "s);
      } else {
        Aimbot.priorityExpireAtMs = -1L;
        Aimbot.sendClientMsg("Priority set to + which + ");
      } 
    }
  }
  
  private static String critsAtClampText() {
    double cStar = (getHeadFracMax() - 0.9D) / 0.2D;
    return fmt3(cStar) + " Crits Max";
  }
  
  private static String fmt3(double v) {
    return String.format(Locale.ROOT, "%.3f", new Object[] { Double.valueOf(v) });
  }
  
  private static final ThreadLocal<DecimalFormat> PCT_FMT3 = new ThreadLocal<DecimalFormat>() {
      protected DecimalFormat initialValue() {
        return new DecimalFormat("0.###");
      }
    };
  
  private static String fmtPct3(double pct) {
    return ((DecimalFormat)PCT_FMT3.get()).format(pct);
  }
  
  private static void sendClientMsg(String s) {
    try {
      if (mc != null && mc.field_71439_g != null)
        mc.field_71439_g.func_145747_a((IChatComponent)new ChatComponentText(s)); 
    } catch (Throwable throwable) {}
  }
  
  @SubscribeEvent
  public void onClientTick(TickEvent.ClientTickEvent e) {
    if (e.phase != TickEvent.Phase.END)
      return; 
    if (mc == null || mc.field_71441_e == null)
      return; 
    if (crits != null) {
      double gv = crits.getValue();
      if (guiCritsLockTicks > 0) {
        guiCritsLockTicks--;
        lastGuiCritsValue = gv;
      } else if (Math.abs(gv - lastGuiCritsValue) > 1.0E-12D) {
        critsOverrideActive = false;
        critsOverrideValue = 0.0D;
        lastGuiCritsValue = gv;
      } 
    } 
    if (predict != null) {
      double gv = predict.getValue();
      if (guiPredictLockTicks > 0) {
        guiPredictLockTicks--;
        lastGuiPredictValue = gv;
      } else if (Math.abs(gv - lastGuiPredictValue) > 1.0E-12D) {
        predictOverrideActive = false;
        predictOverrideValue = 0.0D;
        lastGuiPredictValue = gv;
      } 
    } 
    if (yPredict != null) {
      double gv = yPredict.getValue();
      if (guiYPredictLockTicks > 0) {
        guiYPredictLockTicks--;
        lastGuiYPredictValue = gv;
      } else if (Math.abs(gv - lastGuiYPredictValue) > 1.0E-12D) {
        yPredictOverrideActive = false;
        yPredictOverrideValue = 0.0D;
        lastGuiYPredictValue = gv;
      } 
    } 
    if (vcrits != null) {
      double gv = vcrits.getValue();
      if (guiVCritsLockTicks > 0) {
        guiVCritsLockTicks--;
        lastGuiVCritsValue = gv;
      } else if (Math.abs(gv - lastGuiVCritsValue) > 1.0E-12D) {
        vcritsOverrideActive = false;
        vcritsOverrideValue = 0.0D;
        lastGuiVCritsValue = gv;
      } 
    } 
    if (preFlick != null && preFlick.getValue() && hasLightGrayDyeInThirdSlot((EntityPlayer)mc.field_71439_g)) {
      this.currentTargetId = null;
      this.currentDwellLeft = 0;
      this.preFlickLockExpiresAt = -1L;
      this.postFlickCooldowns.clear();
    } 
    if (!this.cooldownTicks.isEmpty()) {
      Iterator<Map.Entry<Integer, Integer>> it = this.cooldownTicks.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<Integer, Integer> en = it.next();
        int left = ((Integer)en.getValue()).intValue() - 1;
        if (left <= 0) {
          it.remove();
          continue;
        } 
        en.setValue(Integer.valueOf(left));
      } 
    } 
    if (isFlickActive() || isPreFlickActive()) {
      if (this.currentDwellLeft > 0)
        this.currentDwellLeft--; 
    } else {
      this.currentTargetId = null;
      this.currentDwellLeft = 0;
      this.cooldownTicks.clear();
      this.preFlickLockExpiresAt = -1L;
      this.postFlickCooldowns.clear();
    } 
  }
  
  private static void setGuiCritsSlider(double value) {
    if (trySetSlider(crits, value)) {
      guiCritsLockTicks = 2;
      lastGuiCritsValue = value;
    } 
  }
  
  private boolean isFlickActive() {
    return (insta != null && insta
      .getValue() && autoFlick != null && autoFlick
      
      .getValue() && 
      getFlickTicks() > 0 && 
      !blockFlickDueToThirdSlot((EntityPlayer)mc.field_71439_g));
  }
  
  private boolean isPreFlickActive() {
    return (preFlick != null && preFlick
      .getValue() && insta != null && 
      
      !insta.getValue() && 
      !blockPreFlickDueToThirdSlot((EntityPlayer)mc.field_71439_g));
  }
  
  private int getFlickTicks() {
    return (int)Math.max(0L, Math.round((flick == null) ? 0.0D : flick.getValue()));
  }
  
  private boolean hasLightGrayDyeInThirdSlot(EntityPlayer player) {
    if (player == null || player.field_71071_by == null)
      return false; 
    ItemStack[] inv = player.field_71071_by.field_70462_a;
    if (inv == null || inv.length <= 2)
      return false; 
    ItemStack st = inv[2];
    return (st != null && st.func_77973_b() == Items.field_151100_aR && st.func_77952_i() == 7 && st.field_77994_a == 1);
  }
  
  private boolean blockFlickDueToThirdSlot(EntityPlayer player) {
    return hasLightGrayDyeInThirdSlot(player);
  }
  
  private boolean blockPreFlickDueToThirdSlot(EntityPlayer player) {
    return hasLightGrayDyeInThirdSlot(player);
  }
  
  @SubscribeEvent
  public void onRenderWorldLast(RenderWorldLastEvent e) {
    if (onlyFire.getValue() && !mc.field_71474_y.field_74313_G.func_151470_d())
      return; 
    if (mc.field_71439_g == null || mc.field_71441_e == null)
      return; 
    CandidateList cands = collectCandidates(e.partialTicks);
    if (cands.isEmpty()) {
      this.currentTargetId = null;
      this.preFlickLockExpiresAt = -1L;
      return;
    } 
    Integer chosenId = chooseTarget(cands);
    if (chosenId == null)
      return; 
    Vec3 bestAim = cands.aimFor(chosenId);
    if (bestAim != null && pierce != null && pierce.getValue()) {
      Vec3 eyes = mc.field_71439_g.func_174824_e(1.0F);
      Vec3 refined = tryPierceRefineAtChosenTarget(chosenId, eyes);
      if (refined != null)
        bestAim = refined; 
    } 
    if (bestAim != null) {
      Vec3 eyes = mc.field_71439_g.func_174824_e(1.0F);
      float[] ang = calculateYawPitch(eyes, bestAim);
      mc.field_71439_g.field_70177_z = ang[0];
      mc.field_71439_g.field_70125_A = ang[1];
    } 
  }
  
  private Integer chooseTarget(CandidateList cands) {
    if (isPreFlickActive())
      return chooseTargetWithPreFlick(cands); 
    if (isFlickActive())
      return chooseTargetWithFlick(cands); 
    this.currentTargetId = cands.bestId();
    return this.currentTargetId;
  }
  
  private Integer chooseTargetWithFlick(CandidateList cands) {
    int flickTicks = getFlickTicks();
    if (this.currentTargetId != null && this.currentDwellLeft > 0 && cands.contains(this.currentTargetId))
      return this.currentTargetId; 
    Integer bestNonCd = cands.bestIdExcluding(this.cooldownTicks.keySet(), this.currentTargetId);
    if (bestNonCd != null) {
      if (this.currentTargetId != null && !this.currentTargetId.equals(bestNonCd))
        this.cooldownTicks.put(this.currentTargetId, Integer.valueOf(2)); 
      this.currentTargetId = bestNonCd;
      this.currentDwellLeft = flickTicks;
      return this.currentTargetId;
    } 
    if (this.currentTargetId != null && cands.contains(this.currentTargetId)) {
      this.currentDwellLeft = flickTicks;
      return this.currentTargetId;
    } 
    Integer any = cands.bestId();
    this.cooldownTicks.clear();
    this.currentTargetId = any;
    this.currentDwellLeft = flickTicks;
    return this.currentTargetId;
  }
  
  private Integer chooseTargetWithPreFlick(CandidateList cands) {
    long now = System.currentTimeMillis();
    if (this.currentTargetId != null && this.preFlickLockExpiresAt != -1L) {
      if (now < this.preFlickLockExpiresAt && 
        cands.contains(this.currentTargetId))
        return this.currentTargetId; 
      this.postFlickCooldowns.put(this.currentTargetId, Long.valueOf(now + 100L));
      this.preFlickLockExpiresAt = -1L;
      this.currentTargetId = null;
    } 
    Set<Integer> exclusions = new HashSet<>();
    for (Iterator<Map.Entry<Integer, Long>> it = this.postFlickCooldowns.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry<Integer, Long> entry = it.next();
      if (now < ((Long)entry.getValue()).longValue()) {
        exclusions.add(entry.getKey());
        continue;
      } 
      it.remove();
    } 
    Integer newTargetId = cands.bestIdExcluding(exclusions, null);
    if (newTargetId == null) {
      this.currentTargetId = null;
      return null;
    } 
    try {
      EntityLivingBase mob = (EntityLivingBase)mc.field_71441_e.func_73045_a(newTargetId.intValue());
      if (mob != null && mob.func_110143_aJ() <= preFlickHp) {
        this.currentTargetId = newTargetId;
        this.preFlickLockExpiresAt = now + (long)(preFlickSpeedSeconds * 1000.0D);
      } else {
        this.currentTargetId = newTargetId;
      } 
    } catch (Exception ignored) {
      this.currentTargetId = newTargetId;
    } 
    return this.currentTargetId;
  }
  
  private Vec3 tryPierceRefineAtChosenTarget(Integer chosenId, Vec3 eyes) {
    if (chosenId == null || mc == null || mc.field_71441_e == null)
      return null; 
    Entity entity = mc.field_71441_e.func_73045_a(chosenId.intValue());
    if (!(entity instanceof EntityLivingBase))
      return null; 
    EntityLivingBase front = (EntityLivingBase)entity;
    List<EntityLivingBase> others = new ArrayList<>();
    for (Entity e : mc.field_71441_e.field_72996_f) {
      if (e.func_145782_y() != chosenId.intValue() && 
        isCandidate(e))
        others.add((EntityLivingBase)e); 
    } 
    Vec3 bestPierceAim = null;
    double bestScore = Double.POSITIVE_INFINITY;
    for (EntityLivingBase back : others) {
      Vec3 aim = findPierceAimPoint(front, back);
      if (aim != null) {
        double sc = fovDistance(aim);
        if (sc < bestScore) {
          bestScore = sc;
          bestPierceAim = aim;
        } 
      } 
    } 
    return bestPierceAim;
  }
  
  private static class CandidateList {
    private final Map<Integer, Vec3> aimVec = new HashMap<>();
    
    private final Map<Integer, Double> score = new HashMap<>();
    
    private Integer bestId;
    
    boolean isEmpty() {
      return this.score.isEmpty();
    }
    
    boolean contains(Integer id) {
      return (id != null && this.score.containsKey(id));
    }
    
    Vec3 aimFor(Integer id) {
      return (id == null) ? null : this.aimVec.get(id);
    }
    
    Integer bestId() {
      return this.bestId;
    }
    
    Integer bestIdExcluding(Set<Integer> exclude, Integer alsoExclude) {
      Double bestScore = null;
      Integer best = null;
      for (Map.Entry<Integer, Double> en : this.score.entrySet()) {
        Integer id = en.getKey();
        if ((alsoExclude != null && id.equals(alsoExclude)) || (exclude != null && exclude.contains(id)))
          continue; 
        double sc = ((Double)en.getValue()).doubleValue();
        if (bestScore == null || sc < bestScore.doubleValue()) {
          bestScore = Double.valueOf(sc);
          best = id;
        } 
      } 
      return best;
    }
    
    void add(Integer id, Vec3 aim, double sc) {
      if (id == null || aim == null)
        return; 
      Double prev = this.score.get(id);
      if (prev == null || sc < prev.doubleValue()) {
        this.score.put(id, Double.valueOf(sc));
        this.aimVec.put(id, aim);
      } 
      if (this.bestId == null || ((Double)this.score.get(id)).doubleValue() < ((Double)this.score.get(this.bestId)).doubleValue())
        this.bestId = id; 
    }
    
    private CandidateList() {}
  }
  
  private boolean matchesPriority(Entity entity) {
    switch (priorityMode) {
      case GIANT:
        return entity instanceof net.minecraft.entity.monster.EntityGiantZombie;
      case GHAST:
        return entity instanceof net.minecraft.entity.monster.EntityGhast;
      case SENTINEL:
        return (entity instanceof EntityZombie && ((EntityZombie)entity).func_82150_aj());
      case SLIME:
        return (entity instanceof net.minecraft.entity.monster.EntitySlime || entity instanceof net.minecraft.entity.monster.EntityMagmaCube);
      case BLAZE:
        return entity instanceof net.minecraft.entity.monster.EntityBlaze;
    } 
    return false;
  }
  
  private CandidateList collectCandidates(float partialTicks) {
    CandidateList out = new CandidateList();
    Vec3 myEyes = mc.field_71439_g.func_174824_e(1.0F);
    double PIERCE_BONUS = 500.0D;
    double CLOSE_RANGE_BONUS = 2000000.0D;
    double CLOSE_RANGE_THRESHOLD = 25.0D;
    List<EntityLivingBase> validMobs = new ArrayList<>();
    for (Entity entity : mc.field_71441_e.field_72996_f) {
      if (isCandidate(entity) && !(entity instanceof net.minecraft.entity.boss.EntityWither))
        validMobs.add((EntityLivingBase)entity); 
    } 
    boolean anyPriorityFound = false, anyNonPriorityFound = false;
    for (EntityLivingBase living : validMobs) {
      boolean isPriority = isPriorityTarget((Entity)living);
      if (isPriority) {
        anyPriorityFound = true;
        continue;
      } 
      anyNonPriorityFound = true;
    } 
    for (EntityLivingBase living : validMobs) {
      if (creeper != null && creeper.getValue() && living instanceof EntityCreeper) {
        EntityCreeper c = (EntityCreeper)living;
        if (c.func_70830_n()) {
          float hp = c.func_110143_aJ();
          int ticksLived = c.field_70173_aa;
          boolean allowed = false;
          if (hp == 20.0F || hp == 90.0F || hp == 100.0F || hp == 110.0F || hp == 120.0F) {
            allowed = true;
          } else if (hp <= 19.0F && ticksLived >= 10) {
            allowed = true;
          } 
          if (!allowed)
            continue; 
        } 
      } 
      Vec3 lead = getMotionVec((Entity)living, (float)getPredictActive(), (float)getYPredictActive());
      Vec3 aim = findHighestVisibleAimPoint(living, lead, myEyes);
      if (aim == null)
        continue; 
      double sc = fovDistance(aim);
      boolean hasPriorityMatch = matchesPriority((Entity)living);
      if (hasPriorityMatch)
        sc -= 1000000.0D; 
      boolean isOldPriority = isPriorityTarget((Entity)living);
      if (skelePriority.getValue() && anyPriorityFound && isOldPriority)
        sc -= 1000.0D; 
      if (mobPriority.getValue() && anyNonPriorityFound && !isOldPriority)
        sc -= 1000.0D; 
      if (close != null && close.getValue()) {
        double distSq = mc.field_71439_g.func_70068_e((Entity)living);
        boolean canApplyCloseBonus = (priorityMode == PriorityMode.DEFAULT || hasPriorityMatch);
        if (distSq <= 25.0D && canApplyCloseBonus && canWallShot(myEyes, aim)) {
          double closenessBonus = 2000000.0D * (1.0D - distSq / 25.0D);
          sc -= closenessBonus;
        } 
      } 
      out.add(Integer.valueOf(living.func_145782_y()), aim, sc);
    } 
    if (out.isEmpty())
      for (EntityLivingBase living : validMobs) {
        Vec3 lead = getMotionVec((Entity)living, (float)getPredictActive(), (float)getYPredictActive());
        double y = living.field_70163_u + 0.05D;
        Vec3 alt = (new Vec3(living.field_70165_t, y, living.field_70161_v)).func_178787_e(lead);
        if (!canWallShot(myEyes, alt))
          continue; 
        double sc = fovDistance(alt);
        if (matchesPriority((Entity)living))
          sc -= 1000000.0D; 
        out.add(Integer.valueOf(living.func_145782_y()), alt, sc);
      }  
    return out;
  }
  
  private Vec3 findPierceAimPoint(EntityLivingBase mobA, EntityLivingBase mobB) {
    EntityLivingBase frontMob, backMob;
    Dim frontDim, backDim;
    Vec3 frontLead, backLead;
    Dim dimA = mappedDimsOrNull(mobA);
    Dim dimB = mappedDimsOrNull(mobB);
    if (dimA == null || dimB == null)
      return null; 
    double MARGIN = 0.03D;
    Dim safeDimA = new Dim(Math.max(0.03D, dimA.w - 0.03D), Math.max(0.03D, dimA.h - 0.03D), Math.max(0.03D, dimA.d - 0.03D));
    Dim safeDimB = new Dim(Math.max(0.03D, dimB.w - 0.03D), Math.max(0.03D, dimB.h - 0.03D), Math.max(0.03D, dimB.d - 0.03D));
    Vec3 eyes = mc.field_71439_g.func_174824_e(1.0F);
    Vec3 leadA = getMotionVec((Entity)mobA, (float)getPredictActive(), (float)getYPredictActive());
    Vec3 leadB = getMotionVec((Entity)mobB, (float)getPredictActive(), (float)getYPredictActive());
    if (eyes.func_72436_e(mobA.func_174791_d()) < eyes.func_72436_e(mobB.func_174791_d())) {
      frontMob = mobA;
      frontDim = safeDimA;
      frontLead = leadA;
      backMob = mobB;
      backDim = safeDimB;
      backLead = leadB;
    } else {
      frontMob = mobB;
      frontDim = safeDimB;
      frontLead = leadB;
      backMob = mobA;
      backDim = safeDimA;
      backLead = leadA;
    } 
    double startY = frontMob.field_70163_u + frontDim.h;
    double endY = frontMob.field_70163_u;
    List<double[]> xzCols = generateXZColumns(frontMob.field_70165_t, frontMob.field_70161_v, frontDim.w * 0.5D, frontDim.d * 0.5D);
    double y;
    for (y = startY; y >= endY; y -= 0.05D) {
      for (double[] col : xzCols) {
        Vec3 aimPoint = (new Vec3(col[0], y, col[1])).func_178787_e(frontLead);
        Vec3 backHitPoint = getRayHitboxIntersection(eyes, aimPoint, backMob, backDim, backLead);
        if (backHitPoint != null && 
          canWallShot(eyes, aimPoint) && canWallShot(eyes, backHitPoint))
          return aimPoint; 
      } 
    } 
    return null;
  }
  
  private boolean shouldApplyCloseRangePriority(EntityLivingBase entity, Vec3 aim, Vec3 myEyes) {
    if (close == null || !close.getValue())
      return false; 
    if (mc.field_71439_g.func_70068_e((Entity)entity) > 25.0D)
      return false; 
    boolean meetsRequirement = (priorityMode == PriorityMode.DEFAULT || matchesPriority((Entity)entity));
    if (!meetsRequirement)
      return false; 
    return canWallShot(myEyes, aim);
  }
  
  private Vec3 getRayHitboxIntersection(Vec3 start, Vec3 end, EntityLivingBase mob, Dim dim, Vec3 lead) {
    Vec3 mobPos = mob.func_174791_d().func_178787_e(lead);
    double minX = mobPos.field_72450_a - dim.w * 0.5D;
    double maxX = mobPos.field_72450_a + dim.w * 0.5D;
    double minY = mob.field_70163_u;
    double maxY = mob.field_70163_u + dim.h;
    double minZ = mobPos.field_72449_c - dim.d * 0.5D;
    double maxZ = mobPos.field_72449_c + dim.d * 0.5D;
    Vec3 dir = end.func_178788_d(start).func_72432_b();
    if (dir.func_72433_c() < 1.0E-6D)
      return null; 
    double tmin = (minX - start.field_72450_a) / dir.field_72450_a;
    double tmax = (maxX - start.field_72450_a) / dir.field_72450_a;
    if (tmin > tmax) {
      double temp = tmin;
      tmin = tmax;
      tmax = temp;
    } 
    double tymin = (minY - start.field_72448_b) / dir.field_72448_b;
    double tymax = (maxY - start.field_72448_b) / dir.field_72448_b;
    if (tymin > tymax) {
      double temp = tymin;
      tymin = tymax;
      tymax = temp;
    } 
    if (tmin > tymax || tymin > tmax)
      return null; 
    if (tymin > tmin)
      tmin = tymin; 
    if (tymax < tmax)
      tmax = tymax; 
    double tzmin = (minZ - start.field_72449_c) / dir.field_72449_c;
    double tzmax = (maxZ - start.field_72449_c) / dir.field_72449_c;
    if (tzmin > tzmax) {
      double temp = tzmin;
      tzmin = tzmax;
      tzmax = temp;
    } 
    if (tmin > tzmax || tzmin > tmax)
      return null; 
    if (tzmin > tmin)
      tmin = tzmin; 
    if (tzmax < tmax)
      tmax = tymax; 
    if (tmin > 0.0D)
      return start.func_72441_c(dir.field_72450_a * tmin, dir.field_72448_b * tmin, dir.field_72449_c * tmin); 
    return null;
  }
  
  private static class Dim {
    final double w;
    
    final double h;
    
    final double d;
    
    Dim(double w, double h, double d) {
      this.w = w;
      this.h = h;
      this.d = d;
    }
  }
  
  private static Dim getDims(EntityLivingBase e) {
    Dim mapped = mappedDimsOrNull(e);
    if (mapped != null)
      return mapped; 
    double w = aabbW(e);
    double d = aabbD(e);
    double h = aabbHOrEye(e);
    return new Dim(w, h, d);
  }
  
  private static double aabbW(EntityLivingBase e) {
    if (e.func_174813_aQ() != null)
      return Math.max(0.4D, (e.func_174813_aQ()).field_72336_d - (e.func_174813_aQ()).field_72340_a); 
    return 0.8D;
  }
  
  private static double aabbD(EntityLivingBase e) {
    if (e.func_174813_aQ() != null)
      return Math.max(0.4D, (e.func_174813_aQ()).field_72334_f - (e.func_174813_aQ()).field_72339_c); 
    return 0.8D;
  }
  
  private static double aabbHOrEye(EntityLivingBase e) {
    if (e.func_174813_aQ() != null)
      return Math.max(1.4D, (e.func_174813_aQ()).field_72337_e - (e.func_174813_aQ()).field_72338_b); 
    return Math.max(1.8D, e.func_70047_e());
  }
  
  private static double getHeadFracMax() {
    return clamp(USER_HEAD_FRAC_MAX, 0.01D, 2.0D);
  }
  
  private static double getCritsActive() {
    if (critsOverrideActive)
      return critsOverrideValue; 
    return (crits == null) ? 0.0D : crits.getValue();
  }
  
  private static double getPredictActive() {
    return predictOverrideActive ? predictOverrideValue : ((predict == null) ? 0.0D : predict.getValue());
  }
  
  private static double getYPredictActive() {
    return yPredictOverrideActive ? yPredictOverrideValue : ((yPredict == null) ? 0.0D : yPredict.getValue());
  }
  
  private static double getVCritsActive() {
    return vcritsOverrideActive ? vcritsOverrideValue : ((vcrits == null) ? 0.0D : vcrits.getValue());
  }
  
  private double applyVCritsLoweringFeetDelta(double frac, EntityLivingBase target) {
    double k = getVCritsActive();
    if (k <= 0.0D)
      return frac; 
    double dyBlocks = target.field_70163_u - mc.field_71439_g.field_70163_u;
    double lowerPercent = clamp(k * Math.abs(dyBlocks), 0.0D, 0.5D);
    double lowered = frac * (1.0D - lowerPercent);
    return Math.max(lowered, 0.5D);
  }
  
  private Vec3 findHighestVisibleAimPoint(EntityLivingBase e, Vec3 lead, Vec3 eyes) {
    if (lowGiantMode && e instanceof net.minecraft.entity.monster.EntityGiantZombie) {
      Dim dim1 = getDims(e);
      double d1 = e.field_70163_u + 0.01D;
      double topY = e.field_70163_u + dim1.h - 0.01D;
      double yFixed = clamp(e.field_70163_u + 3.0D, d1, topY);
      List<double[]> list = generateXZColumns(e.field_70165_t, e.field_70161_v, dim1.w * 0.5D, dim1.d * 0.5D);
      for (double[] col : list) {
        Vec3 test = (new Vec3(col[0], yFixed, col[1])).func_178787_e(lead);
        if (canWallShot(eyes, test))
          return test; 
      } 
      return null;
    } 
    Dim dim = getDims(e);
    double halfX = dim.w * 0.5D;
    double halfZ = dim.d * 0.5D;
    double baseY = e.field_70163_u + 0.01D;
    double topYHitbox = e.field_70163_u + dim.h - 0.01D;
    double c = getCritsActive();
    double usualHeadFrac = insta.getValue() ? 0.5D : (0.9D + 0.2D * c);
    usualHeadFrac = clamp(usualHeadFrac, 0.05D, getHeadFracMax());
    double headFrac = applyVCritsLoweringFeetDelta(usualHeadFrac, e);
    double headYCandidate = e.field_70163_u + dim.h * headFrac;
    double startY = clamp(headYCandidate, baseY, topYHitbox);
    List<double[]> xzCols = generateXZColumns(e.field_70165_t, e.field_70161_v, halfX, halfZ);
    double bestY = Double.NEGATIVE_INFINITY;
    double bestX = 0.0D, bestZ = 0.0D;
    for (double[] col : xzCols) {
      double colX = col[0], colZ = col[1];
      double y;
      for (y = startY; y >= baseY; y -= 0.05D) {
        Vec3 test = (new Vec3(colX, y, colZ)).func_178787_e(lead);
        if (canWallShot(eyes, test)) {
          if (y > bestY) {
            bestY = y;
            bestX = colX;
            bestZ = colZ;
          } 
          break;
        } 
      } 
    } 
    if (bestY != Double.NEGATIVE_INFINITY)
      return (new Vec3(bestX, bestY, bestZ)).func_178787_e(lead); 
    Vec3 base = (new Vec3(e.field_70165_t, baseY, e.field_70161_v)).func_178787_e(lead);
    return canWallShot(eyes, base) ? base : null;
  }
  
  private List<double[]> generateXZColumns(double cx, double cz, double halfX, double halfZ) {
    List<double[]> out = (List)new ArrayList<>(25);
    out.add(new double[] { cx, cz });
    addRingXZ(out, cx, cz, halfX * 0.35D, halfZ * 0.35D);
    addRingXZ(out, cx, cz, halfX * 0.7D, halfZ * 0.7D);
    addRingXZ(out, cx, cz, halfX * 0.95D, halfZ * 0.95D);
    return out;
  }
  
  private void addRingXZ(List<double[]> out, double cx, double cz, double rx, double rz) {
    out.add(new double[] { cx + rx, cz });
    out.add(new double[] { cx - rx, cz });
    out.add(new double[] { cx, cz + rz });
    out.add(new double[] { cx, cz - rz });
    out.add(new double[] { cx + rx, cz + rz });
    out.add(new double[] { cx - rx, cz + rz });
    out.add(new double[] { cx + rx, cz - rz });
    out.add(new double[] { cx - rx, cz - rz });
  }
  
  private static Dim mappedDimsOrNull(EntityLivingBase e) {
    if (e instanceof EntitySkeleton) {
      EntitySkeleton sk = (EntitySkeleton)e;
      if (sk.func_82202_m() == 1)
        return new Dim(1.0D, 2.6D, 1.0D); 
      return new Dim(0.9D, 2.0D, 0.9D);
    } 
    if (e instanceof EntityZombie) {
      EntityZombie z = (EntityZombie)e;
      if (z.func_70631_g_())
        return new Dim(1.2D, 0.8D, 1.2D); 
      return new Dim(0.9D, 2.0D, 0.9D);
    } 
    if (e instanceof EntityWolf) {
      EntityWolf w = (EntityWolf)e;
      if (w.func_70631_g_())
        return new Dim(0.5D, 0.3D, 0.5D); 
      return new Dim(1.5D, 0.7D, 1.5D);
    } 
    if (e instanceof net.minecraft.entity.monster.EntityPigZombie)
      return new Dim(0.9D, 2.0D, 0.9D); 
    if (e instanceof net.minecraft.entity.monster.EntityBlaze)
      return new Dim(0.9D, 1.1D, 0.9D); 
    if (e instanceof net.minecraft.entity.monster.EntitySilverfish)
      return new Dim(0.2D, 0.4D, 0.2D); 
    if (e instanceof net.minecraft.entity.monster.EntityEndermite)
      return new Dim(0.2D, 0.4D, 0.2D); 
    if (e instanceof net.minecraft.entity.monster.EntityWitch)
      return new Dim(0.9D, 1.75D, 0.9D); 
    if (e instanceof EntityCreeper)
      return new Dim(0.9D, 1.1D, 0.9D); 
    if (e instanceof net.minecraft.entity.monster.EntityCaveSpider)
      return new Dim(0.5D, 0.7D, 0.5D); 
    if (e instanceof net.minecraft.entity.monster.EntityGiantZombie)
      return new Dim(3.9D, 12.0D, 3.9D); 
    if (e instanceof net.minecraft.entity.monster.EntityGhast)
      return new Dim(4.4D, 4.4D, 4.4D); 
    if (e instanceof net.minecraft.entity.monster.EntityIronGolem)
      return new Dim(1.8D, 3.0D, 1.8D); 
    if (e instanceof net.minecraft.entity.monster.EntitySlime || e instanceof net.minecraft.entity.monster.EntityMagmaCube) {
      if (lowSlimeMode)
        return new Dim(0.4D, 0.6D, 0.4D); 
      return null;
    } 
    return null;
  }
  
  private boolean isCandidate(Entity entity) {
    if (!(entity instanceof EntityLivingBase))
      return false; 
    if (!entity.func_70089_S())
      return false; 
    if (entity instanceof net.minecraft.entity.item.EntityArmorStand)
      return false; 
    if (entity instanceof net.minecraft.entity.passive.EntityVillager)
      return false; 
    if (entity instanceof EntityPlayer)
      return false; 
    if (entity instanceof net.minecraft.entity.passive.EntitySquid)
      return false; 
    if (entity instanceof net.minecraft.entity.passive.EntityChicken)
      return false; 
    if (entity instanceof net.minecraft.entity.passive.EntityPig)
      return false; 
    if (entity instanceof net.minecraft.entity.passive.EntityCow)
      return false; 
    if (entity instanceof EntityWolf && !pup.getValue() && ((EntityWolf)entity).func_70631_g_())
      return false; 
    return true;
  }
  
  private boolean isPriorityTarget(Entity entity) {
    return (isPumpkinHead(entity) || isArmoredSkele(entity));
  }
  
  private boolean isPumpkinHead(Entity entity) {
    if (!(entity instanceof EntityLivingBase))
      return false; 
    ItemStack helmet = ((EntityLivingBase)entity).func_71124_b(4);
    return (helmet != null && helmet.func_77973_b() == Item.func_150898_a(Blocks.field_150423_aK));
  }
  
  private boolean isArmoredSkele(Entity entity) {
    if (!(entity instanceof EntitySkeleton))
      return false; 
    EntitySkeleton s = (EntitySkeleton)entity;
    ItemStack boots = s.func_71124_b(1);
    ItemStack legs = s.func_71124_b(2);
    ItemStack chest = s.func_71124_b(3);
    ItemStack held = s.func_70694_bm();
    return (boots != null && boots.func_77973_b() == Items.field_151167_ab && legs != null && legs
      .func_77973_b() == Items.field_151165_aa && chest != null && chest
      .func_77973_b() == Items.field_151030_Z && held != null && held
      .func_77973_b() == Items.field_151052_q);
  }
  
  public static double fovDistance(Vec3 vec3) {
    Vec3 eyes = mc.field_71439_g.func_174791_d().func_72441_c(0.0D, mc.field_71439_g.func_70047_e(), 0.0D);
    float[] a = calculateYawPitch(eyes, vec3);
    return angleBetween(a[0], mc.field_71439_g.field_70177_z) + 
      Math.abs(Math.max(a[1], mc.field_71439_g.field_70125_A) - Math.min(a[1], mc.field_71439_g.field_70125_A));
  }
  
  public static double angleBetween(double first, double second) {
    return Math.abs(subtractAngles(first, second));
  }
  
  public static double subtractAngles(double start, double end) {
    return MathHelper.func_76138_g(end - start);
  }
  
  public static float[] calculateYawPitch(Vec3 start, Vec3 vec) {
    double dx = vec.field_72450_a - start.field_72450_a;
    double dy = vec.field_72448_b - start.field_72448_b;
    double dz = vec.field_72449_c - start.field_72449_c;
    double dxz = Math.sqrt(dx * dx + dz * dz);
    float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
    float pitch = (float)-Math.toDegrees(Math.atan2(dy, dxz));
    return new float[] { MathHelper.func_76142_g(yaw), MathHelper.func_76142_g(pitch) };
  }
  
  public static boolean canWallShot(Vec3 start, Vec3 end) {
    double dx = end.field_72450_a - start.field_72450_a;
    double dy = end.field_72448_b - start.field_72448_b;
    double dz = end.field_72449_c - start.field_72449_c;
    double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
    double eps = 1.0E-7D;
    double sx = start.field_72450_a + ((len > 0.0D) ? (dx / len * eps) : 0.0D);
    double sy = start.field_72448_b + ((len > 0.0D) ? (dy / len * eps) : 0.0D);
    double sz = start.field_72449_c + ((len > 0.0D) ? (dz / len * eps) : 0.0D);
    double ex = end.field_72450_a, ey = end.field_72448_b, ez = end.field_72449_c;
    int x = MathHelper.func_76128_c(sx);
    int y = MathHelper.func_76128_c(sy);
    int z = MathHelper.func_76128_c(sz);
    int endX = MathHelper.func_76128_c(ex);
    int endY = MathHelper.func_76128_c(ey);
    int endZ = MathHelper.func_76128_c(ez);
    int stepX = (dx > 0.0D) ? 1 : ((dx < 0.0D) ? -1 : 0);
    int stepY = (dy > 0.0D) ? 1 : ((dy < 0.0D) ? -1 : 0);
    int stepZ = (dz > 0.0D) ? 1 : ((dz < 0.0D) ? -1 : 0);
    double tDeltaX = (stepX != 0) ? Math.min(Math.abs(1.0D / dx), 1000000.0D) : 1000000.0D;
    double tDeltaY = (stepY != 0) ? Math.min(Math.abs(1.0D / dy), 1000000.0D) : 1000000.0D;
    double tDeltaZ = (stepZ != 0) ? Math.min(Math.abs(1.0D / dz), 1000000.0D) : 1000000.0D;
    double xBorder = (stepX > 0) ? (x + 1) : x;
    double yBorder = (stepY > 0) ? (y + 1) : y;
    double zBorder = (stepZ > 0) ? (z + 1) : z;
    double tMaxX = (stepX != 0) ? ((xBorder - sx) / dx) : 1000000.0D;
    double tMaxY = (stepY != 0) ? ((yBorder - sy) / dy) : 1000000.0D;
    double tMaxZ = (stepZ != 0) ? ((zBorder - sz) / dz) : 1000000.0D;
    int maxIter = 4096;
    while (maxIter-- > 0) {
      Block b = mc.field_71441_e.func_180495_p(new BlockPos(x, y, z)).func_177230_c();
      if (!isAirLike(b))
        return isAllowedFirstSolid(b); 
      if (x == endX && y == endY && z == endZ)
        return true; 
      if (tMaxX < tMaxY) {
        if (tMaxX < tMaxZ) {
          x += stepX;
          tMaxX += tDeltaX;
          continue;
        } 
        z += stepZ;
        tMaxZ += tDeltaZ;
        continue;
      } 
      if (tMaxY < tMaxZ) {
        y += stepY;
        tMaxY += tDeltaY;
        continue;
      } 
      z += stepZ;
      tMaxZ += tDeltaZ;
    } 
    return false;
  }
  
  private static boolean isAirLike(Block b) {
    if (b == Blocks.field_150350_a || b == Blocks.field_150329_H || b == Blocks.field_150395_bd || b == Blocks.field_150404_cg || b == Blocks.field_150431_aC || b == Blocks.field_150479_bC || b == Blocks.field_150430_aB || b == Blocks.field_150471_bO || b == Blocks.field_150442_at)
      return true; 
    try {
      return (b.func_149688_o().func_76224_d() || b.func_149688_o().func_76222_j());
    } catch (Throwable t) {
      return false;
    } 
  }
  
  private static boolean isAllowedFirstSolid(Block b) {
    if (b instanceof BlockSlab) {
      if (((BlockSlab)b).func_176552_j())
        return false; 
      return true;
    } 
    if (b instanceof net.minecraft.block.BlockStairs) {
      if (b == Blocks.field_150481_bH)
        return false; 
      if (b == Blocks.field_150401_cl)
        return false; 
      if (b == Blocks.field_150485_bF)
        return false; 
      if (b == Blocks.field_150372_bz)
        return false; 
      if (b == Blocks.field_150387_bl)
        return false; 
      if (b == Blocks.field_180396_cN)
        return false; 
      return true;
    } 
    if (b == Blocks.field_150454_av)
      return true; 
    if (b == Blocks.field_150411_aY)
      return true; 
    if (b instanceof net.minecraft.block.BlockSign)
      return true; 
    if (b instanceof net.minecraft.block.BlockBarrier)
      return true; 
    if (b == Blocks.field_180407_aO)
      return true; 
    return false;
  }
  
  public static Vec3 getMotionVec(Entity entity, float ticks, float yTicks) {
    double dX = entity.field_70165_t - entity.field_70169_q;
    double dY = entity.field_70163_u - entity.field_70167_r;
    double dZ = entity.field_70161_v - entity.field_70166_s;
    double outX = 0.0D, outY = 0.0D, outZ = 0.0D;
    double i;
    for (i = 1.0D; i <= ticks; i += 0.3D) {
      double i2;
      for (i2 = 1.0D; i2 <= yTicks && 
        !mc.field_71441_e.func_72829_c(entity.func_174813_aQ().func_72317_d(dX * i, dY * i2, dZ * i)); i2 += 0.3D) {
        outX = dX * i;
        outY = dY * i2;
        outZ = dZ * i;
      } 
    } 
    return new Vec3(outX, outY, outZ);
  }
  
  private static String normalizeChat(String s) {
    if (s == null)
      return ""; 
    String out = s.toLowerCase();
    out = out.replaceAll("[\\[\\](){}|:;*~_^<>, " ");
    out = out.replaceAll("\\s+", " ").trim();
    return out;
  }
  
  private static boolean trySetBoolean(BooleanSetting setting, boolean value) {
    if (setting == null)
      return false; 
    Class<?> c = setting.getClass();
    String[] methods = { "setValue", "set", "setEnabled", "setState", "setToggled", "setObject" };
    for (String m : methods) {
      try {
        c.getMethod(m, new Class[] { boolean.class }).invoke(setting, new Object[] { Boolean.valueOf(value) });
        return true;
      } catch (NoSuchMethodException noSuchMethodException) {
        try {
          c.getMethod(m, new Class[] { Boolean.class }).invoke(setting, new Object[] { Boolean.valueOf(value) });
          return true;
        } catch (NoSuchMethodException noSuchMethodException1) {}
      } catch (Throwable throwable) {}
    } 
    try {
      Object cur = c.getMethod("getValue", new Class[0]).invoke(setting, new Object[0]);
      boolean isOn = (cur instanceof Boolean) ? ((Boolean)cur).booleanValue() : false;
      if (isOn != value)
        c.getMethod("toggle", new Class[0]).invoke(setting, new Object[0]); 
      return true;
    } catch (Throwable throwable) {
      String[] fields = { "value", "enabled", "state", "toggled" };
      for (String f : fields) {
        try {
          Field fld = c.getDeclaredField(f);
          fld.setAccessible(true);
          fld.set(setting, Boolean.valueOf(value));
          return true;
        } catch (Throwable throwable1) {}
      } 
      return false;
    } 
  }
  
  private static boolean trySetSlider(SliderSetting setting, double value) {
    if (setting == null)
      return false; 
    Class<?> c = setting.getClass();
    try {
      c.getMethod("setValue", new Class[] { double.class }).invoke(setting, new Object[] { Double.valueOf(value) });
      return true;
    } catch (NoSuchMethodException noSuchMethodException) {
      try {
        c.getMethod("setValue", new Class[] { Double.class }).invoke(setting, new Object[] { Double.valueOf(value) });
        return true;
      } catch (NoSuchMethodException noSuchMethodException1) {}
    } catch (Throwable throwable) {}
    try {
      Field fld = c.getDeclaredField("value");
      fld.setAccessible(true);
      fld.set(setting, Double.valueOf(value));
      return true;
    } catch (Throwable throwable) {
      return false;
    } 
  }
  
  private static double clamp(double v, double lo, double hi) {
    return (v < lo) ? lo : ((v > hi) ? hi : v);
  }
  
  private static float clamp(float v, float lo, float hi) {
    return (v < lo) ? lo : ((v > hi) ? hi : v);
  }
}

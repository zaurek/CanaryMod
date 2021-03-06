import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Logger;



public class ONetLoginHandler extends ONetHandler {

    public static Logger a = Logger.getLogger("Minecraft");
    private static Random d = new Random();
    public ONetworkManager b;
    public boolean c = false;
    private OMinecraftServer e;
    private int f = 0;
    private String g = null;
    private OPacket1Login h = null;
    private String i = "";
    
    private String worldname; // CanaryMod: store worldname given by plugins

    public ONetLoginHandler(OMinecraftServer var1, Socket var2, String var3) throws IOException {
        super();
        this.e = var1;
        this.b = new ONetworkManager(var2, var3, this);
        this.b.f = 0;
    }

    public void a() {
        if (this.h != null) {
            this.b(this.h);
            this.h = null;
        }

        if (this.f++ == 600) {
            this.a("Took too long to log in");
        } else {
            this.b.b();
        }

    }

    public void a(String var1) {
        try {
            a.info("Disconnecting " + this.b() + ": " + var1);
            this.b.a((OPacket) (new OPacket255KickDisconnect(var1)));
            this.b.d();
            this.c = true;
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }

    public void a(OPacket2Handshake var1) {
        if (this.e.n) {
            this.i = Long.toString(d.nextLong(), 16);
            this.b.a((OPacket) (new OPacket2Handshake(this.i)));
        } else {
            this.b.a((OPacket) (new OPacket2Handshake("-")));
        }

    }

    public void a(OPacket1Login var1) {
        //CanaryMod: Filter bad player names and remove them from the login process
        if(!var1.b.toLowerCase().matches("[a-z0-9-_]+")) {
            c=true; //finished processing
            b.a("This name has been assimilated and you have been kicked.");
            return;
        }
        //CanaryMod End
        this.g = var1.b;
        if (var1.a != 29) {
            if (var1.a > 29) {
                this.a("Outdated server!");
            } else {
                this.a("Outdated client!");
            }

        } else {
            if (!this.e.n) {
                this.b(var1);
            } else {
                (new OThreadLoginVerifier(this, var1)).start();
            }

        }
    }

    public void b(OPacket1Login var1) {
        OEntityPlayerMP var2 = this.e.h.a(this, var1.b); //create new player instance - this has called a loginchecks hook

        if (var2 != null) { //Is not null, lets go on!
            this.e.h.b(var2);
            //The world the player will spawn in is set here.
            //We had the LoginChecks hook in this.e.h.a(this, var1.b); so we have a specific world
            //already specified and only get the right dimension here if that's needed.
            var2.a((OWorld) this.e.getWorld(var2.bi.name, var2.dimension)); 
            var2.c.a((OWorldServer) var2.bi);
            a.info(this.b() + " logged in with entity id " + var2.bd + " at (" + var2.bm + ", " + var2.bn + ", " + var2.bo + " in world "+var2.bi.name+". Dimension: "+var2.dimension+")");
            OWorldServer var3 = this.e.getWorld(var2.bi.name, var2.dimension);
            OChunkCoordinates var4 = var3.p();

            var2.c.b(var3.s().m());
            ONetServerHandler var5 = new ONetServerHandler(this.e, this.b, var2);
            
            // CanaryMod - if seed is hidden send 0 instead.
            var5.b((OPacket) (new OPacket1Login("", var2.bd, var3.s().p(), var2.c.a(), var3.t.g, (byte) var3.q, (byte) var3.y(), (byte) this.e.h.k())));
            var5.b((OPacket) (new OPacket6SpawnPosition(var4.a, var4.b, var4.c)));
            this.e.h.a(var2, var3);
            // CanaryMod - onPlayerConnect Hook
            HookParametersConnect hookResult = new HookParametersConnect(String.format(Colors.Yellow + "%s joined the game.", var2.v), true);

            hookResult = (HookParametersConnect) etc.getLoader().callHook(PluginLoader.Hook.PLAYER_CONNECT, var2.getPlayer(), hookResult);
            if (!hookResult.isHidden()) { 
                this.e.h.a((OPacket) (new OPacket3Chat(hookResult.getJoinMessage())));
            }
            
            // CanaryMod - Check Creative Mode
            var2.getPlayer().refreshCreativeMode();
            
            // CanaryMod - Check if player is listed as muted, and mute him
            if(etc.getDataSource().isPlayerOnMuteList(var2.getPlayer().getName())) {
                var2.getPlayer().toggleMute();
            }
            // CanaryMod END

            this.e.h.a(var2, var3);
            //this.e.h.a((OPacket) (new OPacket3Chat("\u00a7e" + var2.v + " joined the game.")));
            this.e.h.c(var2);
            var5.a(var2.bm, var2.bn, var2.bo, var2.bs, var2.bt, var2.dimension, var2.bi.name);
            this.e.c.a(var5);
            var5.b((OPacket) (new OPacket4UpdateTime(var3.o())));

            // CanaryMod - enable/disable potion effects on login
            if (hookResult.applyPotionsEffects()) {
                Iterator var6 = var2.aM().iterator();

                while (var6.hasNext()) {
                    OPotionEffect var7 = (OPotionEffect) var6.next();

                    var5.b((OPacket) (new OPacket41EntityEffect(var2.bd, var7)));
                }
            }

            var2.x();
        }

        this.c = true;
    }

    public void a(String var1, Object[] var2) {
        a.info(this.b() + " lost connection");
        this.c = true;
    }

    public void a(OPacket254ServerPing var1) {
        if (this.b.f() == null) {
            return;
        } // CanaryMod - Fix if we don't have a socket, don't do anything
        try {
            String var2 = this.e.s + "\u00a7" + this.e.h.j() + "\u00a7" + this.e.h.k();

            this.b.a((OPacket) (new OPacket255KickDisconnect(var2)));
            // CanaryMod swapped lines below. The network connection should be terminated AFTER removing the socket from the connection list.
            this.e.c.a(this.b.f());
            this.b.d();
            this.c = true;
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }

    public void a(OPacket var1) {
        this.a("Protocol error");
    }

    public String b() {
        return this.g != null ? this.g + " [" + this.b.c().toString() + "]" : this.b.c().toString();
    }

    public boolean c() {
        return true;
    }

    // $FF: synthetic method
    static String a(ONetLoginHandler var0) {
        return var0.i;
    }

    // $FF: synthetic method
    static OPacket1Login a(ONetLoginHandler var0, OPacket1Login var1) {
        return var0.h = var1;
    }

}

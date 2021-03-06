/**
 * 
 * @author
 */
public class HumanEntity extends LivingEntity {

    /**
     * Constructor
     */
    public HumanEntity() {}

    /**
     * Constructor
     * 
     * @param human
     */
    public HumanEntity(OEntityPlayer human) {
        super(human);
    }

    /**
     * Returns the entity we're wrapping.
     * 
     * @return
     */
    @Override
    public OEntityPlayer getEntity() {
        return (OEntityPlayer) entity;
    }

    /**
     * Returns the name
     * 
     * @return
     */
    @Override
    public String getName() {
        return getEntity().v;
    }

    /**
     * Returns the world
     * 
     * @return
     */
    @Override
    public World getWorld() {
        return etc.getMCServer().getWorld(getEntity().bi.name, getEntity().dimension).world;
    }
}

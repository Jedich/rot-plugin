package tld.sofugames.data;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bed;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tld.sofugames.models.ClaimedChunk;
import tld.sofugames.models.House;
import tld.sofugames.models.King;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Data {
	public static final int basicFee = 2;
	private static Data instance = null;
	//key Chunk.toString()
	public HashMap<String, ClaimedChunk> claimData = new HashMap<>();
	//key UUID.toString()
	public HashMap<String, King> kingData = new HashMap<>();
	//key bedBlock.toString()
	public HashMap<String, House> houseData = new HashMap<>();
	public Connection connection;
	public int lastClaim = 1, lastKing = 1, lastHouse = 1;

	public static Data getInstance() {
		if(instance == null) {
			instance = new Data();
		}
		return instance;
	}

	public String getBedHash(Block bedBlock) {
		Bed bed = (Bed) bedBlock.getBlockData();
		Block res;
		if (bed.getPart() == Bed.Part.HEAD) {
			res = bedBlock;
		} else {
			res = bedBlock.getRelative(bed.getFacing());
		}
		return "BedBlock{x=" + res.getX() + ",y=" + res.getY() + ",z=" + res.getZ() + "}";
	}

	public void giveBed(Player player) {
		ItemStack bed = new ItemStack(Tag.BEDS.getValues().
				stream()
				.skip(new Random().nextInt(
						Tag.BEDS.getValues().size())).findFirst().orElse(Material.WHITE_BED));
		ItemMeta itemMeta = bed.getItemMeta();
		itemMeta.setDisplayName("Claim Bed");
		itemMeta.setLore(Arrays.asList("Use this to claim an enclosed living space."));
		itemMeta.addEnchant(Enchantment.LUCK, 0, true);
		bed.setItemMeta(itemMeta);
		player.getInventory().addItem(bed);
	}

	public BlockFace[] faces = new BlockFace[]{BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN};

	public EnumSet<Material> ignoreList = Stream.of(Tag.ANVIL, Tag.CARPETS, Tag.CROPS,
			Tag.BUTTONS, Tag.BANNERS, Tag.BEDS, Tag.RAILS, Tag.FLOWERS,
			Tag.FIRE, Tag.CLIMBABLE, Tag.PRESSURE_PLATES, Tag.SIGNS)
			.map(Tag::getValues)
			.flatMap(Set::stream)
			.collect(Collectors.toCollection(() -> EnumSet.noneOf(Material.class)));

	public EnumSet<Material> benefitList = Stream.of(Tag.CARPETS, Tag.CLIMBABLE, Tag.FLOWER_POTS, Tag.FLOWERS,
			Tag.CAMPFIRES, Tag.SLABS, Tag.TRAPDOORS, Tag.STAIRS, Tag.WOOL, Tag.LOGS, Tag.BANNERS, Tag.WALL_SIGNS)
			.map(Tag::getValues)
			.flatMap(Set::stream)
			.collect(Collectors.toCollection(() -> EnumSet.noneOf(Material.class)));


	public int getLastClaim() {
		return lastClaim++;
	}

	public int getLastKing() {
		return lastKing++;
	}

	public int getLastHouse() {
		return lastHouse++;
	}
}
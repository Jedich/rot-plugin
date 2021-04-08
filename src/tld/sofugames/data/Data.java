package tld.sofugames.data;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import tld.sofugames.models.ClaimedChunk;
import tld.sofugames.models.King;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Data {
	public static final int basicFee = 2;
	private static Data instance = null;
	public HashMap<String, ClaimedChunk> claimData = new HashMap<>();
	public HashMap<String, King> kingData = new HashMap<>();
	public Connection connection;
	public int lastClaim = 1, lastKing = 1;

	public static Data getInstance() {
		if(instance == null) {
			instance = new Data();
		}
		return instance;
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
}
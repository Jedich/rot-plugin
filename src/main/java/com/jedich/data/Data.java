package com.jedich.data;

import com.jedich.models.King;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bed;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Data {
	private static Data instance = null;
	public Plugin plugin;
	public HashMap<UUID, King> kingdomRequests = new HashMap<>();
	public static final int MIN_CLAIM_HEIGHT = 40;

	public Connection getConnection() {
		try {
			if(connection == null || connection.isClosed() || !connection.isValid(5000)) {
				System.out.println("connecting to db...");
				if(connection != null) connection.close();
				final Properties prop = new Properties();
				prop.setProperty("useSSL", "false"); //Set to true if you have a SSL installed to your database (?)
				prop.setProperty("autoReconnect", "true");
				connection = DriverManager.getConnection("jdbc:sqlite:plugins/RoT-Reloaded/rotr.db", prop);
				System.out.println(getClass().getClassLoader().getResourceAsStream("init.sql"));
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(getClass().getClassLoader().getResourceAsStream("init.sql")));
				StringBuilder lines = new StringBuilder();
				String strLines;
				String line;
				int lineNum = 0;
				while((line = bufferedReader.readLine()) != null) {
					if(lineNum == 0) {
						if(line.length() < 15) {
							continue;
						}
					}
					lines.append(line);
					lineNum++;
				}
				strLines = lines.toString();
				Statement statement = connection.createStatement();
				statement.setQueryTimeout(30);
				statement.executeUpdate(strLines);
				return connection;
			}
		} catch(SQLException | IOException e) {
			e.printStackTrace();
		}
		return connection;
	}

	private Connection connection;

	public static Data getInstance() {
		if(instance == null) {
			instance = new Data();
		}
		return instance;
	}

	public void setPlugin(Plugin plugin) {
		getInstance().plugin = plugin;
	}

	public String getBedHash(Block bedBlock) {
		Bed bed = (Bed) bedBlock.getBlockData();
		Block res;
		if(bed.getPart() == Bed.Part.HEAD) {
			res = bedBlock;
		} else {
			res = bedBlock.getRelative(bed.getFacing());
		}
		return "BedBlock{x=" + res.getX() + ",y=" + res.getY() + ",z=" + res.getZ() + "}";
	}

	public void giveBed(Player player, boolean toCheck) {
		if(toCheck) {
			for(Material bed : Tag.BEDS.getValues()) {
				if(player.getInventory().contains(bed)) return;
			}
		}
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

	public HashMap<UUID, Long> timers = new HashMap<>();

	public EnumSet<Material> ignoreList = Stream.of(Tag.ANVIL, Tag.CARPETS, Tag.CROPS,
					Tag.BUTTONS, Tag.BANNERS, Tag.BEDS, Tag.RAILS, Tag.FLOWERS,
					Tag.FIRE, Tag.CLIMBABLE, Tag.PRESSURE_PLATES, Tag.SIGNS, Tag.CROPS, Tag.SAPLINGS)
			.map(Tag::getValues)
			.flatMap(Set::stream)
			.collect(Collectors.toCollection(() -> EnumSet.noneOf(Material.class)));

	public EnumSet<Material> benefitList = Stream.of(Tag.CARPETS, Tag.CLIMBABLE, Tag.FLOWER_POTS, Tag.FLOWERS,
					Tag.CAMPFIRES, Tag.SLABS, Tag.TRAPDOORS, Tag.STAIRS, Tag.WOOL, Tag.LOGS, Tag.BANNERS, Tag.WALL_SIGNS)
			.map(Tag::getValues)
			.flatMap(Set::stream)
			.collect(Collectors.toCollection(() -> EnumSet.noneOf(Material.class)));

	public static List<Integer> colors = Stream.of(
			0x2176ff, 0x42f4f1, 0xff9900,
			0x2ab849, 0x842ab8, 0xdb0000,
			0xe0e0e0, 0x66411e, 0xffe600, 0xc8ff69).collect(Collectors.toList());
	public static Map<String, Integer> kingMapColor = new HashMap<>();

	Stream<String> titleStream = Stream.of("Kind", "Bastard", "Hunter", "Fearless", "Terrible", "Bold", "Brave",
			"Gracious", "Ruthless", "Headless", "Evil", "Abomination", "Trader", "Tiny", "Gentle", "Bald",
			"java.lang.TooMuchEvilException", "Wise", "Bear", "Legendary", "Architect", "Builder");
	public String[] titles = titleStream.map(name -> ", " + ChatColor.GOLD + "the " + name).toArray(String[]::new);

	public String getRomanNumber(int number) {
		StringBuilder iChars = new StringBuilder();
		for(int i = 0; i < number; i++) {
			iChars.append("I");
		}
		return String.join("", iChars.toString())
				.replace("IIIII", "V")
				.replace("IIII", "IV")
				.replace("VV", "X")
				.replace("VIV", "IX")
				.replace("XXXXX", "L")
				.replace("XXXX", "XL")
				.replace("LL", "C")
				.replace("LXL", "XC")
				.replace("CCCCC", "D")
				.replace("CCCC", "CD")
				.replace("DD", "M")
				.replace("DCD", "CM");
	}

	private static Image heightMap;

	public static int GetHighestBlockHeightmap(int x, int z) {

		Image map = heightMap;

		if(map.heightMap.length == 0) {
			return MIN_CLAIM_HEIGHT;
		}
		x += map.width/2;
		z += map.height/2;
		if(map.width < x ||  map.height < z) {
			return MIN_CLAIM_HEIGHT;
		}
		//System.out.printf("Player on %d, %d: height %d %n", x, z, map.heightMap[z * map.width + x] - 64);
		return (int) map.heightMap[z * map.width + x] - 64;
	}

	public static void LoadHeightmap(Plugin plugin) {
		Image result;
		try {
			BufferedImage hugeImage = ImageIO.read(new File(plugin.getDataFolder() + "/heightmap.png"));
			result = ConvertToHeightmap(hugeImage);
		} catch(IOException e) {
			System.out.println("World height map was not provided, skipping...");
			return;
		}
		System.out.printf("World height map loaded, size: %dx%d%n", result.width, result.height);
		heightMap = result;
	}

	private static Image ConvertToHeightmap(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		short[] heightMap = new short[width * height];
		image.getRaster().getDataElements(0, 0, width, height, heightMap);
		return new Image(height, width, heightMap);
	}

}

class Image{
	int width, height;
	short[] heightMap;
	short[] getHeightMap() {
		return heightMap;
	}

	public Image(int height, int width, short[] heightMap) {
		this.heightMap = heightMap;
		this.height = height;
		this.width = width;
	}
}
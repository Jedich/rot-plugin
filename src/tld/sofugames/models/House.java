package tld.sofugames.models;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import tld.sofugames.data.Data;
import tld.sofugames.rot.HousingOutOfBoundsException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

public class House {

	private int id;
	public UUID owner;
	public String bedBlockId;
	public int level = 1;
	public int area;
	public int benefits;
	public float income;
	public Block bedBlock;

	public House(UUID owner, String bedBlockId, Block bedBlock) {
		this.owner = owner;
		this.bedBlockId = bedBlockId;
		this.bedBlock = bedBlock;
		calculateIncome();
	}

	public House(int id, UUID owner, String bedBlockId, Block bedBlock, int area, int benefits, float income) {
		this.id = id;
		this.owner = owner;
		this.bedBlockId = bedBlockId;
		this.area = area;
		this.benefits = benefits;
		this.income = income;
		this.bedBlock = bedBlock;
	}

	public House() { }

	private boolean hasCeiling(Block current, int counter) {
		if(counter > 15) return false;

		if(current.getRelative(BlockFace.UP).getType() == Material.AIR) {
			counter++;
			return hasCeiling(current.getRelative(BlockFace.UP), counter);
		} else {
			return true;
		}
	}

	public boolean isEnclosed() throws HousingOutOfBoundsException {
		if(hasCeiling(this.bedBlock, 0)) {
			this.area = 0;
			this.benefits = 0;
			if(allDirectionSearch(this.bedBlock, new HashMap<>())) {
				if(this.area > 2) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private boolean allDirectionSearch(Block currentBlock, HashMap<String, Block> visitedList)
			throws HousingOutOfBoundsException {
		for(BlockFace face : Data.getInstance().faces) {
			Block rel = currentBlock.getRelative(face);
			if(!visitedList.containsKey(rel.toString())) {
				visitedList.put(rel.toString(), rel);
				if(rel.getType() == Material.AIR || rel.getType() == Material.CAVE_AIR || rel.getType() == Material.TORCH //*********************************
						|| Data.getInstance().ignoreList.contains(rel.getType())) {
					if(Tag.BEDS.getValues().contains(rel.getType())) {
						if(Data.getInstance().houseData.containsKey(Data.getInstance().getBedHash(rel))) {
							if(Data.getInstance().houseData.get(Data.getInstance().getBedHash(rel)) != this) {
								throw new HousingOutOfBoundsException("House is already claimed!");
							}
						}
					}
					if(this.area > 150) {
						return false;
					}
					this.area++;
					allDirectionSearch(rel, visitedList);
				} else if(Data.getInstance().benefitList.contains(rel.getType())) {
					this.benefits++;
				}
			}
		}
		return this.area <= 150;
	}

	private void calculateIncome() {
		float a = 1 + benefits * 0.025f;
		income = (float) (4 * a * Math.sin(0.024 * area - 7.9) + 4 * a);
		Data.getInstance().kingData.get(owner.toString()).changeIncome(income);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}

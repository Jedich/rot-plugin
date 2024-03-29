package com.jedich.models;

import com.jedich.dao.impl.HouseDao;
import com.jedich.data.Data;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import com.jedich.dao.impl.DaoFactory;
import com.jedich.rot.HousingOutOfBoundsException;

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

	public House() {
	}

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
		HouseDao houseData = new DaoFactory().getHouses();
		for(BlockFace face : Data.getInstance().faces) {
			Block rel = currentBlock.getRelative(face);
			if(!visitedList.containsKey(rel.toString())) {
				visitedList.put(rel.toString(), rel);
				if(Data.getInstance().ignoreList.contains(rel.getType())) {
					if(Tag.BEDS.getValues().contains(rel.getType())) {
						if(houseData.get(Data.getInstance().getBedHash(rel)).isPresent()) {
							if(houseData.get(Data.getInstance().getBedHash(rel)).get() != this) {
								throw new HousingOutOfBoundsException("House is already claimed!");
							}
						}
					}
					if(this.area > 250) {
						return false;
					}
					this.area++;
					allDirectionSearch(rel, visitedList);
				} else if(Data.getInstance().benefitList.contains(rel.getType())) {
					this.benefits++;
				}
			}
		}
		return this.area <= 250;
	}

	public void calculateIncome() {
		if(benefits > 100) benefits = 100;
		float a = 1 + benefits * 0.004f;
		income = (float) (a * 3 * Math.log(area + 12) - 8);
		//income = (float) (4 * a * (Math.sin(0.01255 * area - 7.859) + 1));
		new DaoFactory().getKings().get(owner.toString()).orElse(new King()).changeIncome(income);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}

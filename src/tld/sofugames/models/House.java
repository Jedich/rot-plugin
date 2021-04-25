package tld.sofugames.models;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import tld.sofugames.data.Data;
import tld.sofugames.rot.HousingOutOfBoundsException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class House implements Model {

	public int id;
	public UUID owner;
	public String bedBlock;
	private int level = 1;
	public int area;
	public int benefits;
	public float income;

	public House(int id, UUID owner, String bedBlock) {
		this.id = id;
		this.owner = owner;
		this.bedBlock = bedBlock;
	}

	public House(int id, UUID owner, String bedBlock, int area, int benefits, float income) {
		this.id = id;
		this.owner = owner;
		this.bedBlock = bedBlock;
		this.area = area;
		this.benefits = benefits;
		this.income = income;
	}

	public boolean hasCeiling(Block current, int counter) {
		if(counter > 15) return false;

		if(current.getRelative(BlockFace.UP).getType() == Material.AIR) {
			counter++;
			return hasCeiling(current.getRelative(BlockFace.UP), counter);
		} else {
			return true;
		}
	}

	public boolean isEnclosed(Block bedBlock) throws HousingOutOfBoundsException {
		if(hasCeiling(bedBlock, 0)) {
			return allDirectionSearch(bedBlock, new HashMap<>());
		} else {
			return false;
		}
	}

	public boolean allDirectionSearch(Block currentBlock, HashMap<String, Block> visitedList)
			throws HousingOutOfBoundsException {
		//if (startBed == null) startBed = currentBlock;
		for(BlockFace face : Data.getInstance().faces) {
			Block rel = currentBlock.getRelative(face);
			if(!visitedList.containsKey(rel.toString())) {
				visitedList.put(rel.toString(), rel);
				if(rel.getType() == Material.AIR || rel.getType() == Material.CAVE_AIR || rel.getType() == Material.TORCH //*********************************
						|| Data.getInstance().ignoreList.contains(rel.getType())) {
					if(Tag.BEDS.getValues().contains(rel.getType())) {
						if(Data.getInstance().houseData.containsKey(Data.getInstance().getBedHash(rel))) {
							throw new HousingOutOfBoundsException("House is already claimed!");
						}
					}
					this.area++;
					if(this.area > 150) {
						return false;
						//throw new HousingOutOfBoundsException("House area doesn't match the rules: Size can't be > 150");
					}
					allDirectionSearch(rel, visitedList);
//					House tempHouse = allDirectionSearch(rel, visitedList, thisHouse, startBed);
//					if (tempHouse.area == 0) {
//						throw new HousingOutOfBoundsException("House area doesn't match the rules: Size can't be > 150");
//					} else {
//						thisHouse.area = tempHouse.area;
//					}
				} else if(Data.getInstance().benefitList.contains(rel.getType())) {
					this.benefits++;
				}
			}
		}
		return this.area > 2;
	}

	private void calculateIncome() {
		float a = 1 + benefits * 0.025f;
		income = (float) (4 * a * Math.sin(0.024 * area - 7.9) + 4 * a);
		Data.getInstance().kingData.get(owner.toString()).changeIncome(income);
	}

	@Override
	public boolean pushToDb(Connection connection) throws SQLException {
		calculateIncome();
		PreparedStatement pstmt = (PreparedStatement) connection.prepareStatement(
				"INSERT INTO houses VALUES(?, ?, ?, ?, ?, ?, ?)");
		pstmt.setInt(1, id);
		pstmt.setString(2, owner.toString());
		pstmt.setString(3, bedBlock);
		pstmt.setInt(4, level);
		pstmt.setInt(5, area);
		pstmt.setInt(6, benefits);
		pstmt.setFloat(7, income);
		pstmt.executeUpdate();
		System.out.println(pstmt.toString());
		return true;
	}

	@Override
	public boolean readFromDb(Connection connection) throws SQLException {
		return false;
	}

	public void delete(Connection connection) throws SQLException {
		PreparedStatement pstmt = connection.prepareStatement("DELETE FROM houses WHERE id = ?");
		pstmt.setInt(1, id);
		pstmt.executeUpdate();
		Data.getInstance().houseData.remove(bedBlock);
		Data.getInstance().kingData.get(owner.toString()).changeIncome(-income);
	}
}

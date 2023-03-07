package com.jedich.dynmap;

import com.jedich.dao.impl.DaoFactory;
import com.jedich.dao.impl.KingDao;
import com.jedich.data.Data;
import com.jedich.models.ClaimedChunk;
import com.jedich.models.King;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;

import java.util.Random;

public class Dynmap {
	public static DynmapAPI dapi = null;
	private static DaoFactory daoFactory = new DaoFactory();
	private static KingDao kingData = daoFactory.getKings();

	public static void paintChunk(ClaimedChunk chunk) {
		MarkerSet ms = dapi.getMarkerAPI().getMarkerSet(chunk.owner.toString());
		AreaMarker am = ms.findAreaMarker(chunk.chunkId);
		if(am == null) am = ms.createAreaMarker(chunk.chunkId, kingData.get(chunk.owner.toString()).orElse(null).kingdomName, true,
				"rot-newgen", new double[]{chunk.x*16, chunk.x*16+16}, new double[]{chunk.z*16, chunk.z*16+16}, false);
		am.setFillStyle(0.5, Data.kingMapColor.get(chunk.owner.toString()));
		am.setLineStyle(1, 0.6, darkenColor(Data.kingMapColor.get(chunk.owner.toString()), 0.5f));
	}

	public static void changeLabel(King king, String label) {
		dapi.getMarkerAPI().getMarkerSet(king.rawName.toString()).setMarkerSetLabel(label);
	}

	public static void removeChunk(ClaimedChunk chunk) {
		dapi.getMarkerAPI().getMarkerSet(chunk.owner.toString()).findAreaMarker(chunk.chunkId).deleteMarker();
	}

	public static void addKing(King king) {
		MarkerSet ms = dapi.getMarkerAPI().getMarkerSet(king.rawName.toString());
		if(ms == null) ms = dapi.getMarkerAPI().createMarkerSet(king.rawName.toString(), king.kingdomName, dapi.getMarkerAPI().getMarkerIcons(), false);
		int id = new Random().nextInt(Data.colors.size());
		Data.kingMapColor.put(king.rawName.toString(), Data.colors.get(id));
		Data.colors.remove(id);
	}

	private static int darkenColor(int colorInt, float factor) {
		int red = (int) ((colorInt >> 16) & 0xFF);
		int green = (int) ((colorInt >> 8) & 0xFF);
		int blue = (int) (colorInt & 0xFF);

		red = (int) (red * (1 - factor));
		green = (int) (green * (1 - factor));
		blue = (int) (blue * (1 - factor));

		return (red << 16) | (green << 8) | blue;
	}
}

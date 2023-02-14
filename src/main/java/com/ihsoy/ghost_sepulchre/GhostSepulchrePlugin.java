package com.ihsoy.ghost_sepulchre;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import javax.inject.Inject;

import com.ihsoy.ghost_sepulchre.recording.Playback;
import com.ihsoy.ghost_sepulchre.recording.Recording;
import com.ihsoy.ghost_sepulchre.recording.RecordingHandler;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@PluginDescriptor(
	name = "Ghost Sepulchre"
)
public class GhostSepulchrePlugin extends Plugin
{
	private static final String CONFIG_GROUP = "groundMarker";
	private static final String RECORDING_KEY = "recording";
	private static final Set<Integer> HALLOWED_SEPULCHRE_MAP_REGIONS = ImmutableSet.of(8797, 10077, 9308, 10074, 9050); // one map region per floor
	private static final int MAX_TILES_PER_RECORDING = 500;
	private static final String SENDER = "Ghost Sepulchre";
	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private GhostSepulchreOverlay overlay;
	@Inject
	private GhostSepulchreConfig config;
	@Inject
	private RecordingHandler recordingHandler;
	@Inject
	private ConfigManager configManager;

	private Recording currentRecording;
	public Playback currentPlayback;

	public final static int SEPULCHRE_TIMER = 10413;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Ghost Sepulchre started!");
		overlayManager.add(overlay);
		loadRecordings();
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Ghost Sepulchre stopped!");
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged) {
		if(varbitChanged.getVarbitId() == SEPULCHRE_TIMER) {
			if(varbitChanged.getValue() == 1) {
				stopGhost();
			}
		}
		if(varbitChanged.getVarbitId() == 6719) {
			if(varbitChanged.getValue() == 0) {
				startGhost();
			} else {
				resetGhost();
			}
		}
	}

	private void saveRecordings() {
		String json = recordingHandler.toJson();
		configManager.setConfiguration(CONFIG_GROUP, RECORDING_KEY, json);
		log.info("Recordings Saved: {}", recordingHandler.recordingCount());
	}

	private void loadRecordings() {
		String json = configManager.getConfiguration(CONFIG_GROUP, RECORDING_KEY);
		recordingHandler.loadJson(json);
		log.info("Recordings Loaded: {}", recordingHandler.recordingCount());
	}

	private void deleteRecordings() {
		log.info("Recording deleted");
		configManager.setConfiguration(CONFIG_GROUP, RECORDING_KEY, "");
		recordingHandler.deleteRecordings();
	}
	private void resetGhost() {
		log.info("RESET");
		currentRecording = null;
		currentPlayback = null;
	}

	private boolean isPlayerInSepulchre()
	{
		final int[] mapRegions = client.getMapRegions();

		for (int region : mapRegions)
		{
			if (HALLOWED_SEPULCHRE_MAP_REGIONS.contains(region))
			{
				return true;
			}
		}

		return false;
	}

	private void startGhost() {
		if(!isPlayerInSepulchre()) {
			log.info("NOT IN SEPULCHRE");
			return;
		}
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Run started", null);

		LocalPoint currentLocation = client.getLocalPlayer().getLocalLocation();

		currentRecording = recordingHandler.createRecording(currentLocation);
		currentRecording.startRecording();

		Recording record = recordingHandler.findRecording(currentLocation);
		if(record != null) {
			currentPlayback = record.createPlayback();
			currentPlayback.translateToWorldPoint(client);
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Ghost recording for this track found!", null);
		}
	}

	private void stopGhost() {
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Run Stopped", SENDER);
		currentRecording.addCurrentTile(client);
		currentRecording.stopRecording();
		RecordingHandler.RecordResult result = recordingHandler.storeRecording(currentRecording);
		printEndMessage(result);
		resetGhost();
		saveRecordings();
	}
	private void printEndMessage(RecordingHandler.RecordResult result) {
		double recording = TimeUnit.MILLISECONDS.convert(currentRecording.getRunTime(), TimeUnit.NANOSECONDS) / 1000.0;
		double playback = 0;
		if(currentPlayback != null) {
			playback = TimeUnit.MILLISECONDS.convert(currentPlayback.getRecording().getRunTime(), TimeUnit.NANOSECONDS) / 1000.0;
		}
		switch (result) {
			case FIRST:
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ef1020>Ghost recorded. It took you </col>" + recording + " seconds<col=ef1020> to complete this floor!", null);
				break;
			case SLOWER:
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ef1020>You were slower than the Ghost. It took you </col>" + recording + " seconds<col=ef1020> and the Ghost </col>" + playback + " seconds<col=ef1020> to complete this floor!</col>", null);
				break;
			case FASTER:
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ef1020>You were faster than the Ghost. It took you </col>" + recording + " seconds<col=ef1020> and the Ghost </col>" + playback + " seconds<col=ef1020> to complete this floor!</col>", null);
				break;
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
	}

	@Subscribe
	public void	onConfigChanged(ConfigChanged configChanged) {
		if(configChanged.getKey().equals("reset") && configChanged.getNewValue().equals("true")) {
			//configChanged.setNewValue("false");
			configManager.setConfiguration(configChanged.getGroup(), configChanged.getProfile(), configChanged.getKey(), false);
			deleteRecordings();
			//configManager.setConfiguration(configChanged.getGroup(), configChanged.getProfile(), configChanged.getKey(), "false");
			//configManager.unsetConfiguration(configChanged.getGroup(), configChanged.getKey());
			log.info(configChanged.getNewValue());
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick) {
		if(currentRecording != null) {
			currentRecording.addCurrentTile(client);
			if(currentRecording.count() > MAX_TILES_PER_RECORDING) {
				resetGhost();
			}
			if(currentPlayback != null && !currentPlayback.isDone()) {
				currentPlayback.nextPoint();
				if(currentPlayback.isDone()) {
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You were too slow! The ghost reached the end in.", SENDER);
				}
			}
		}
	}

	@Provides
	GhostSepulchreConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GhostSepulchreConfig.class);
	}
}

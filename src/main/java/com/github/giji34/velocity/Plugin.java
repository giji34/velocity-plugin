package com.github.giji34.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.player.TabListEntry;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@com.velocitypowered.api.plugin.Plugin(id = "giji34_velocity_plugin", name = "Giji34VelocityPlugin", version = "1.0", description = "A velocity plugin for giji34", authors = { "kbinani" })
public class Plugin {
  private final ProxyServer server;
  private final Logger logger;

  @Inject
  public Plugin(ProxyServer server, Logger logger) {
    this.server = server;
    this.logger = logger;
    logger.info("Loading giji34_velocity_plugin");
  }

  @Subscribe
  public void onLeave(DisconnectEvent event) {
    Player left = event.getPlayer();
    for (Player player : server.getAllPlayers()) {
      if (player.getUniqueId().equals(left.getUniqueId())) {
        continue;
      }
      TabList list = player.getTabList();
      list.removeEntry(left.getUniqueId());
    }
  }

  @Subscribe
  public void onProxyInitialize(ProxyInitializeEvent event) {
    server
        .getScheduler()
        .buildTask(this, this::updateTabList)
        .repeat(1, TimeUnit.SECONDS)
        .delay(1, TimeUnit.SECONDS)
        .schedule();
  }

  private void updateTabList() {
    try {
      unsafeUpdateTabList();
    } catch (Exception e) {
      logger.warn(e.getMessage());
    }
  }

  private void unsafeUpdateTabList() {
    Player[] players = server.getAllPlayers().toArray(new Player[0]);
    for (Player target : players) {
      TabList list = target.getTabList();
      for (Player current : players) {
        if (target.equals(current)) {
          continue;
        }
        if (IsInSameServer(target, current)) {
          continue;
        }
        TabListEntry entry = null;
        for (TabListEntry item : list.getEntries()) {
          if (item.getProfile().getId().equals(current.getUniqueId())) {
            entry = item;
            break;
          }
        }
        if (entry == null) {
          entry = TabListEntry.builder()
              .profile(current.getGameProfile())
              .displayName(Component.text(current.getUsername()))
              .gameMode(3)
              .tabList(list)
              .build();
          list.addEntry(entry);
        } else {
          entry.setDisplayName(Component.text(current.getUsername()));
          entry.setGameMode(3);
        }
      }
    }
  }

  private static boolean IsInSameServer(Player a, Player b) {
    Optional<ServerConnection> serverA = a.getCurrentServer();
    Optional<ServerConnection> serverB = b.getCurrentServer();
    if (!serverA.isPresent() || !serverB.isPresent()) {
      return false;
    }
    String nameA = serverA.get().getServerInfo().getName();
    String nameB = serverB.get().getServerInfo().getName();
    return nameA.equals(nameB);
  }
}

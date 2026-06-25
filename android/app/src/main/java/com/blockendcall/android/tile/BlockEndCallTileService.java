package com.blockendcall.android.tile;

import android.app.role.RoleManager;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import androidx.annotation.RequiresApi;
import com.blockendcall.android.ui.MainActivity;

@RequiresApi(api = Build.VERSION_CODES.N)
public class BlockEndCallTileService extends TileService {

    @Override
    public void onTileAdded() { updateTile(); }

    @Override
    public void onStartListening() { updateTile(); }

    @Override
    public void onClick() {
        Tile tile = getQsTile();
        if (tile == null) return;
        if (tile.getState() == Tile.STATE_ACTIVE) {
            tile.setState(Tile.STATE_INACTIVE);
            tile.updateTile();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityAndCollapse(intent);
        }
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager rm = (RoleManager) getSystemService(ROLE_SERVICE);
            boolean active = rm != null && rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING);
            tile.setState(active ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        }
        tile.setLabel("BlockEndCall");
        tile.updateTile();
    }
}

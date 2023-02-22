package sn.zhang.deskAround.client;

import android.content.Intent;
import android.net.Uri;
import android.service.quicksettings.TileService;


public class CastTileService extends TileService {
    @Override
    public void onClick() {
        super.onClick();
        startActivityAndCollapse(new Intent().setData(Uri.parse("castto://5C593F65-CEE2-4696-ADD4-09CBB8B5480B/C39B773A-847B-4129-9BB9-AAA9D494995A?type=TYPE_EXTERNAL_PROVIDER_PREFERRED")).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}

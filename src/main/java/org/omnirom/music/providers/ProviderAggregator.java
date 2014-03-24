package org.omnirom.music.providers;

import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.omnirom.music.model.Album;
import org.omnirom.music.model.Artist;
import org.omnirom.music.model.Playlist;
import org.omnirom.music.model.Song;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProviderAggregator extends IProviderCallback.Stub {

    private static final String TAG = "ProviderAggregator";

    private List<ILocalCallback> mUpdateCallbacks;
    private List<IMusicProvider> mProviders;
    private ProviderCache mCache;
    private Handler mHandler;

    // Singleton
    private final static ProviderAggregator INSTANCE = new ProviderAggregator();
    public final static ProviderAggregator getDefault() {
        return INSTANCE;
    }
    ////////////

    private ProviderAggregator() {
        mUpdateCallbacks = new ArrayList<ILocalCallback>();
        mProviders = new ArrayList<IMusicProvider>();
        mCache = new ProviderCache();
        mHandler = new Handler();
    }

    public ProviderCache getCache() {
        return mCache;
    }

    public void addUpdateCallback(ILocalCallback cb) {
        mUpdateCallbacks.add(cb);
    }

    public void removeUpdateCallback(ILocalCallback cb) {
        mUpdateCallbacks.remove(cb);
    }

    public Iterator<ILocalCallback> getUpdateCallbacks() {
        return mUpdateCallbacks.iterator();
    }

    public void registerProvider(IMusicProvider provider) {
        if (!mProviders.contains(provider)) {
            mProviders.add(provider);
            try {
                provider.registerCallback(this);
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to register as a callback");
            }
        }
    }

    public void search(final String query, final ISearchCallback callback) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void onLoggedIn(final IMusicProvider provider, boolean request) throws RemoteException {
        // Request playlists if we logged in
        Log.d(TAG, "onLoggedIn(" + request + ")");
        if (request) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<Playlist> pls = provider.getPlaylists();

                        if (pls != null) {
                            Log.d(TAG, "getPlaylists: returned " + pls.size() + " playlists");
                            for (Playlist pl : pls) {
                                onPlaylistAddedOrUpdated(provider, pl);
                            }
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "Error while calling getPlaylist", e);
                    }
                }
            });
        } else {
            // TODO: Show a message
        }
    }

    @Override
    public void onLoggedOut(IMusicProvider provider) throws RemoteException {

    }

    @Override
    public void onPlaylistAddedOrUpdated(IMusicProvider provider, Playlist p) throws RemoteException {
        Log.d(TAG, "onPlaylistAddedOrUpdated: " + p);
        mCache.putPlaylist(p);
    }

    @Override
    public void onSongUpdate(IMusicProvider provider, Song s) throws RemoteException {

    }

    @Override
    public void onAlbumUpdate(IMusicProvider provider, Album a) throws RemoteException {

    }

    @Override
    public void onArtistUpdate(IMusicProvider provider, Artist a) throws RemoteException {

    }


}
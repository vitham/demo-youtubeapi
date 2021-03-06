package com.vit.demoyoutubeapi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

public class PlayVideoActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener{

    private String mVideoId;
    private String mUrlGetJson;

    @BindView(R.id.view_youtube) YouTubePlayerView mViewYoutube;
    @BindView(R.id.list_relate_video) ListView mListVideo;

    private ArrayList<VideoYoutube> mVideoYoutubeList;
    private VideoYoutubeAdapter mVideoYoutubeAdapter;

    int REQUEST_VIDEO = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        ButterKnife.bind(this);

        mVideoId = getIntent().getStringExtra("videoId");
        mUrlGetJson = "https://www.googleapis.com/youtube/v3/search?part=snippet&relatedToVideoId="
                + mVideoId +"&type=video&key=" + Constant.API_KEY + "&maxResults=" + Constant.MAX_RESULTS;

        mViewYoutube.initialize(Constant.API_KEY, this);

        mVideoYoutubeList = new ArrayList<>();
        mVideoYoutubeAdapter = new VideoYoutubeAdapter(this, R.layout.item_video_youtube, mVideoYoutubeList);
        mListVideo.setAdapter(mVideoYoutubeAdapter);

        getJsonYoutube(mUrlGetJson);
    }

    @OnItemClick(R.id.list_relate_video)
    void onItemClickList(AdapterView<?> parent, int position) {
        Intent intent = new Intent(PlayVideoActivity.this, PlayVideoActivity.class);
        intent.putExtra("videoId", mVideoYoutubeList.get(position).getVideoId());
        startActivity(intent);
    }


    private void getJsonYoutube(String url) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray jsonItems = response.getJSONArray("items");

                            String videoId = "", title = "", url = "";

                            for(int i = 0; i < jsonItems.length(); i++) {
                                JSONObject jsonItem = jsonItems.getJSONObject(i);

                                JSONObject jsonId = jsonItem.getJSONObject("id");
                                JSONObject jsonSnippet = jsonItem.getJSONObject("snippet");

                                videoId = jsonId.getString("videoId");
                                title = jsonSnippet.getString("title");

                                JSONObject jsonThumbnail = jsonSnippet.getJSONObject("thumbnails");
                                JSONObject jsonMedium = jsonThumbnail.getJSONObject("medium");
                                url = jsonMedium.getString("url");

                                mVideoYoutubeList.add(new VideoYoutube(title, url, videoId));
                            }

                            mVideoYoutubeAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PlayVideoActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        youTubePlayer.loadVideo(mVideoId);
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult result) {
        if (result.isUserRecoverableError()) {
            result.getErrorDialog(PlayVideoActivity.this, REQUEST_VIDEO);
        } else {
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_VIDEO) {
            mViewYoutube.initialize(Constant.API_KEY, PlayVideoActivity.this);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

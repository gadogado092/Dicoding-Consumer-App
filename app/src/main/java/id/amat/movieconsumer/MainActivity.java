package id.amat.movieconsumer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class MainActivity extends AppCompatActivity {

    private static final int LOADER_MOVIE = 1;

    /** The authority of this content provider. */
    public static final String AUTHORITY = "id.amat.dmovie.provider.MovieProvider";

    public static final String TABEL_MOVIE = "tbmovie";

    public static final String COLUMN_TITLE = "original_title";
    public static final String COLUMN_PATH = "poster_path";
    public static final String COLUMN_DATE = "release_date";
    public static final String COLUMN_OVERVIEW = "overview";
    public static final String COLUMN_ADULT = "adult";
    public static final String COLUMN_GENRE = "genre";

    /** The URI for the Movie table. */
    public static final Uri URI_MOVIE = Uri.parse(
            "content://"+ AUTHORITY + "/"+ TABEL_MOVIE);

    private MovieAdapter movieAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        movieAdapter = new MovieAdapter();
        RecyclerView recyclerView = findViewById(R.id.rv_movie);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(movieAdapter);
        recyclerView.setHasFixedSize(true);

        LoaderManager.getInstance(this).initLoader(LOADER_MOVIE, null, mLoaderCallbacks);

    }

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @NonNull
                @Override
                public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
                    switch (id){
                        case LOADER_MOVIE:
                            return new CursorLoader(getApplicationContext(),
                                    URI_MOVIE, new String[] {COLUMN_TITLE, COLUMN_PATH, COLUMN_ADULT, COLUMN_DATE, COLUMN_OVERVIEW, COLUMN_GENRE},
                                    null, null, null
                                    );
                        default:
                            throw new IllegalArgumentException();
                    }
                }

                @Override
                public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
                    switch (loader.getId()) {
                        case LOADER_MOVIE:
                            movieAdapter.setMovie(data, MainActivity.this);
                            break;
                    }
                }

                @Override
                public void onLoaderReset(@NonNull Loader<Cursor> loader) {
                    switch (loader.getId()) {
                        case LOADER_MOVIE:
                            movieAdapter.setMovie(null, MainActivity.this);
                            break;
                    }
                }
            };

    private static class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder>{

        private Cursor mCursor;
        private Context mContext;

        @NonNull
        @Override
        public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemRow = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);
            return new MovieViewHolder(itemRow);
        }

        void setMovie(Cursor cursor, Context context) {
            mCursor = cursor;
            mContext = context;
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(@NonNull final MovieViewHolder holder, int position) {
            if (mCursor.moveToPosition(position)) {
                String title = checkTextIfNull(mCursor.getString(mCursor.getColumnIndexOrThrow(MainActivity.COLUMN_TITLE)));
                String date = checkTextIfNull(mCursor.getString(mCursor.getColumnIndexOrThrow(MainActivity.COLUMN_DATE)));
                String overview = checkTextIfNull(mCursor.getString(mCursor.getColumnIndexOrThrow(MainActivity.COLUMN_OVERVIEW)));
                String poster = checkTextIfNull(mCursor.getString(mCursor.getColumnIndexOrThrow(MainActivity.COLUMN_PATH)));

                if (title.length() > 30) {
                    holder.tvTitle.setText(String.format("%s...", title.substring(0, 29)));
                } else {
                    holder.tvTitle.setText(title);
                }

                holder.tvOverview.setText(overview);
                holder.tvDateRelease.setText(date);

                Glide.with(mContext)
                        .load("https://image.tmdb.org/t/p/w92"+poster)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                holder.progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                holder.progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .error(R.drawable.ic_broken_image_black_24dp)
                        .into(holder.imgPoster);
            }
        }

        String checkTextIfNull(String text) {
            if (text != null && !text.isEmpty()) {
                return text;
            } else {
                return "-";
            }
        }

        @Override
        public int getItemCount() {
            return mCursor == null ? 0 : mCursor.getCount();
        }

        public class MovieViewHolder extends RecyclerView.ViewHolder {

            TextView tvTitle;
            TextView tvAdult;
            TextView tvDateRelease;
            TextView tvOverview;
            ImageView imgPoster;
            ProgressBar progressBar;

            public MovieViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.txt_title);
                tvDateRelease = itemView.findViewById(R.id.txt_date_release);
                tvOverview = itemView.findViewById(R.id.txt_overview);
                tvAdult = itemView.findViewById(R.id.txt_adult);
                imgPoster = itemView.findViewById(R.id.img_poster);
                progressBar = itemView.findViewById(R.id.progressBar);
            }


        }
    }
}

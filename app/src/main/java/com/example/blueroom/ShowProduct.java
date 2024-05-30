package com.example.blueroom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.IOException;
import java.util.ArrayList;

public class ShowProduct extends Fragment {

    private static final String TAG = "ShowProduct";

    private ArrayList<products> cartProducts = new ArrayList<>();

    private String author;
    private String imageUrl;
    private String name;
    private float price;
    private int quantity;
    private int date;
    private String type;
    private String musicName;
    private ArrayList<String> tag;
    private String audioUrl;  // Nueva variable para el URL del audio
    SharedPreferences favoritesPreferences;

    private NavController navController;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Runnable updateSeekBar;

    public ShowProduct() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            author = getArguments().getString("author", "");
            imageUrl = getArguments().getString("imageurl", "");
            name = getArguments().getString("name", "");
            price = getArguments().getFloat("price", 0);
            date = getArguments().getInt("date", 0); // Obtener el valor de date aquí
            type = getArguments().getString("type", "");
            tag = getArguments().getStringArrayList("tag");
            audioUrl = getArguments().getString("musicurl", ""); // Obtener el valor del audio URL aquí
            musicName = getArguments().getString("musicname", ""); // Obtener el valor del nombre de la canción
            Log.d(TAG, "Music URL: " + audioUrl);
        }

        MyApp myApp = (MyApp) requireActivity().getApplication();
        cartProducts = myApp.getCartProducts();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_product, container, false);

        TextView authorTextView = view.findViewById(R.id.author);
        ImageView imageView = view.findViewById(R.id.image);
        TextView nameTextView = view.findViewById(R.id.name);
        TextView priceTextView = view.findViewById(R.id.price);
        TextView typeTextView = view.findViewById(R.id.type);
        TextView tagTextView = view.findViewById(R.id.tag);
        TextView dateTextView = view.findViewById(R.id.date);
        TextView songNameTextView = view.findViewById(R.id.songname); // Obtener la referencia del TextView para el nombre de la canción
        SeekBar audioSeekBar = view.findViewById(R.id.audioSeekBar);
        ImageButton playPauseButton = view.findViewById(R.id.play_pause_button);

        authorTextView.setText(author);
        nameTextView.setText(name);
        String formattedPrice = String.format("%.2f€", price);
        priceTextView.setText(formattedPrice);
        typeTextView.setText(type);
        dateTextView.setText(String.valueOf(date));
        songNameTextView.setText(musicName); // Establecer el nombre de la canción aquí
        if (tag != null && !tag.isEmpty()) {
            StringBuilder tagBuilder = new StringBuilder();
            for (String tagItem : tag) {
                tagBuilder.append(tagItem).append(", ");
            }
            if (tagBuilder.length() > 0) {
                tagBuilder.deleteCharAt(tagBuilder.length() - 2);
            }
            tagTextView.setText(tagBuilder.toString());
        }

        Glide.with(requireContext()).load(imageUrl).into(imageView);

        // Configurar el MediaPlayer
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            // Crear una Uri a partir del enlace de Firebase
            Uri audioUri = Uri.parse(audioUrl);
            mediaPlayer.setDataSource(requireContext(), audioUri);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, "Error setting data source: " + e.getMessage());
        }

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                audioSeekBar.setMax(mediaPlayer.getDuration());
                playPauseButton.setEnabled(true);
                // Music is not started automatically anymore
                playPauseButton.setImageResource(R.drawable.baseline_play_arrow_24);
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playPauseButton.setImageResource(R.drawable.baseline_play_arrow_24);
                audioSeekBar.setProgress(0);
            }
        });

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playPauseButton.setImageResource(R.drawable.baseline_play_arrow_24);
                } else {
                    mediaPlayer.start();
                    playPauseButton.setImageResource(R.drawable.baseline_pause_24);
                    handler.post(updateSeekBar);
                }
            }
        });

        audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    audioSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                    handler.postDelayed(this, 1000);
                }
            }
        };

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        RecyclerView recyclerView = view.findViewById(R.id.related_recyclerview);

        Button buyButton = view.findViewById(R.id.buy);
        buyButton.setOnClickListener(v -> {
            MyApp myApp = (MyApp) requireActivity().getApplication();
            Log.d(TAG, "Adding product to cart: " + name);
            products newProduct = new products(imageUrl, name, author, price, quantity);
            myApp.addProductToCart(newProduct);  // Usar la función para añadir producto

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Product Added to the Cart")
                    .setCancelable(false) // Prevents dismissal by tapping outside
                    .setPositiveButton("Continue", (dialog, id) -> {
                        // No action here. Just dismisses the dialog.
                    });

            AlertDialog alert = builder.create();
            alert.show();
        });

        favoritesPreferences = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE);
        SharedPreferences preferences = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE);

        boolean isFavorite = preferences.getBoolean(name, false);

        ImageButton favoriteButton = view.findViewById(R.id.favorite_button);
        favoriteButton.setOnClickListener(v -> toggleFavoriteStatus());
        setFavoriteStatus(isFavorite);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        String currentProductId = getArguments().getString("name");
        ArrayList<String> originalTags = getArguments().getStringArrayList("tag");

        Query query = FirebaseFirestore.getInstance().collection("products");
        ArrayList<Query> tagQueries = new ArrayList<>();

        for (String tag : originalTags) {
            Query tagQuery = query.whereArrayContains("tag", tag);
            tagQueries.add(tagQuery);
        }

        if (!tagQueries.isEmpty()) {
            query = tagQueries.get(0);
            for (int i = 1; i < tagQueries.size(); i++) {
                query = FirebaseFirestore.getInstance().collectionGroup("products").whereEqualTo("tag", tagQueries.get(i));
            }
        }

        query = query.whereNotEqualTo("name", currentProductId);

        FirestoreRecyclerOptions<products> options = new FirestoreRecyclerOptions.Builder<products>()
                .setQuery(query, products.class)
                .setLifecycleOwner(this)
                .build();
        ProductAdapter.OnProductClickListener listener = product -> {
            Bundle bundle = new Bundle();
            bundle.putString("author", product.getAuthor());
            bundle.putString("imageurl", product.getImageurl());
            bundle.putString("name", product.getName());
            bundle.putFloat("price", product.getPrice());
            bundle.putFloat("quantity", product.getQuantity()); // Corrected type to int
            bundle.putInt("date", product.getDate()); // Asegúrate de incluir date aquí
            bundle.putString("type", product.getType());
            bundle.putString("musicurl", product.getMusicurl());
            bundle.putString("musicname", product.getMusicName()); // Incluye el nombre de la canción
            bundle.putStringArrayList("tag", new ArrayList<>(product.getTag()));
            stopMediaPlayer();
            navController.navigate(R.id.showProduct, bundle);
        };

        ProductAdapter adapter = new ProductAdapter(options, listener);
        recyclerView.setAdapter(adapter);
    }

    private void openCartFragment() {
        navController.navigate(R.id.cartFragment);
    }

    private void toggleFavoriteStatus() {
        // Load current favorite status
        boolean isFavorite = favoritesPreferences.getBoolean(name, false);
        isFavorite = !isFavorite; // Toggle favorite status

        // Save updated favorite status
        SharedPreferences.Editor editor = favoritesPreferences.edit();
        editor.putBoolean(name, isFavorite);
        editor.apply();

        // Update UI
        setFavoriteButtonImage(isFavorite);
    }

    private void setFavoriteStatus(boolean isFavorite) {
        setFavoriteButtonImage(isFavorite);
    }

    private void setFavoriteButtonImage(boolean isFavorite) {
        ImageButton favoriteButton = getView().findViewById(R.id.favorite_button);
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.fav);
        } else {
            favoriteButton.setImageResource(R.drawable.baseline_star_outline_24);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopMediaPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMediaPlayer();
    }

    private void stopMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateSeekBar);
    }
}

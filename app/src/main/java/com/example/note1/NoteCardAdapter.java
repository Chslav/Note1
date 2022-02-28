package com.example.note1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class NoteCardAdapter extends
        RecyclerView.Adapter<NoteCardAdapter.ViewHolder> {

    /* private String[][] notes;
     * notes[1] - text for cardView (200 char)
     * notes[2] - filepath
     * notes[3] - date for last change */
    private String[] textForCardView;
    private String[] filePath;
    private String[] dateLastModifier;
    private static String[] deleteFlag;

    public NoteCardAdapter(String[][] filesInfo) {
        super();
        textForCardView = filesInfo[0];
        filePath = filesInfo[1];
        dateLastModifier = filesInfo[2];
        deleteFlag = filesInfo[3];
    }

    @Override
    public NoteCardAdapter.ViewHolder onCreateViewHolder(
            ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_note, parent, false);
        return new ViewHolder(cv);
    }

    @Override
    public int getItemCount(){
        return textForCardView.length;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position){
        CardView cardView = holder.cardView;

        TextView textView = (TextView)cardView.findViewById(R.id.text_text);
        TextView dateView = (TextView)cardView.findViewById(R.id.date_text);

        textView.setText(textForCardView[position]);
        dateView.setText(dateLastModifier[position]);

        if (deleteFlag[position].equals ("while")) {
            cardView.setBackgroundResource (R.drawable.border_white);
        } else {
            cardView.setBackgroundResource (R.drawable.border_orange);
        }

        cardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(cardView.getContext(), EditNoteActivity.class);
                intent.putExtra(EditNoteActivity.EXTRA_NOTE_PATH, filePath[position]);
                cardView.getContext().startActivity(intent);
            }
        });

        //отметить записку для удаления
        cardView.setOnLongClickListener (new View.OnLongClickListener () {
            public boolean onLongClick (View v) {
                if (deleteFlag[position].equals ("while")) {
                    v.setBackgroundResource (R.drawable.border_orange);
                    deleteFlag[position] = filePath[position];
                } else {
                    v.setBackgroundResource (R.drawable.border_white);
                    deleteFlag[position] ="while";
                }
                return true;
            }
        });
    }

    //метод для передачи массива отмеченных файлов deleteFlag в MainActivity
    protected static String[] getDeleteFlag() {
        return deleteFlag;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;

        public ViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }
}

package apps.youravgjoe.com.animals;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivityFragment extends Fragment {

    private static final String TAG = "MainActivityFragment";

    private static final String A = "a";
    private static final String AN = "an";

    private DBHandler mDb;

    private LinearLayout mStartGameLayout;
    private LinearLayout mQuestionLayout;
    private LinearLayout mNewAnimalLayout;
    private LinearLayout mNewQuestionLayout;
    private LinearLayout mQuestionYesNoLayout;
    private LinearLayout mPlayAgainLayout;

    private Button mStartGame;
    private Button mYes;
    private Button mNo;
    private Button mSubmitNewAnimal;
    private Button mSubmitNewQuestion;
    private Button mQuestionYes;
    private Button mQuestionNo;
    private Button mPlayAgain;
    private Button mQuit;

    private TextView mQuestion;
    private TextView mNewQuestionPrompt;
    private TextView mQuestionYesNoPrompt;
    private TextView mPlayAgainPrompt;

    private EditText mNewAnimalEditText;
    private EditText mNewQuestionEditText;

    private Node mCurrentNode;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mDb = new DBHandler(getActivity());

        // seed the database
        if (mDb.getRowCount() == 0) {
            // initialize this with the yesId pointing to frog and the noId pointing to moose
            mDb.addRow(new Node(0, "Does it live in the water?", null, 1, 2));
            mDb.addRow(new Node(1, String.format(getResources().getString(R.string.guess_animal), A, "frog"), "frog"));
            mDb.addRow(new Node(2, String.format(getResources().getString(R.string.guess_animal), A, "moose"), "moose"));
        }

        // setup views and click listeners
        setupViews(rootView);
        setupOnClickListeners();

        // start at the top of the tree
        mCurrentNode = mDb.getRow(0);

        return rootView;
    }

    private void setupViews(View rootView) {
        // layouts (for showing and hiding)
        mStartGameLayout = (LinearLayout) rootView.findViewById(R.id.start_game_layout);
        mQuestionLayout = (LinearLayout) rootView.findViewById(R.id.question_layout);
        mNewAnimalLayout = (LinearLayout) rootView.findViewById(R.id.new_animal_layout);
        mNewQuestionLayout = (LinearLayout) rootView.findViewById(R.id.new_question_layout);
        mQuestionYesNoLayout = (LinearLayout) rootView.findViewById(R.id.question_yes_no_layout);
        mPlayAgainLayout = (LinearLayout) rootView.findViewById(R.id.play_again_layout);

        // buttons
        mStartGame = (Button) rootView.findViewById(R.id.start_game);
        mYes = (Button) rootView.findViewById(R.id.yes);
        mNo = (Button) rootView.findViewById(R.id.no);
        mSubmitNewAnimal = (Button) rootView.findViewById(R.id.submit_new_animal);
        mSubmitNewQuestion = (Button) rootView.findViewById(R.id.submit_new_question);
        mQuestionYes = (Button) rootView.findViewById(R.id.question_yes);
        mQuestionNo = (Button) rootView.findViewById(R.id.question_no);
        mPlayAgain = (Button) rootView.findViewById(R.id.play_again);
        mQuit = (Button) rootView.findViewById(R.id.quit);

        // text views
        mQuestion = (TextView) rootView.findViewById(R.id.question);
        mNewQuestionPrompt = (TextView) rootView.findViewById(R.id.new_question_prompt);
        mQuestionYesNoPrompt = (TextView) rootView.findViewById(R.id.question_yes_no_prompt);
        mPlayAgainPrompt = (TextView) rootView.findViewById(R.id.play_again_prompt);

        // edit texts
        mNewAnimalEditText = (EditText) rootView.findViewById(R.id.new_animal);
        mNewQuestionEditText = (EditText) rootView.findViewById(R.id.new_question);
    }

    private void setupOnClickListeners() {
        mStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStartGameLayout.setVisibility(View.GONE);
                mQuestionLayout.setVisibility(View.VISIBLE);
                mQuestion.setText(mCurrentNode.getQuestion());
            }
        });
        mYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentNode.getYesId() == -1) {
                    // the only reason the yes id should be -1 is if it's a "is it a moose?" kind of question.
                    // we guessed it!
                    mQuestionLayout.setVisibility(View.GONE);
                    mPlayAgainPrompt.setText(getResources().getString(R.string.prompt_play_again_guessed_it));
                    mPlayAgainLayout.setVisibility(View.VISIBLE);
                } else {
                    // move down the yes line
                    mCurrentNode = mDb.getRow(mCurrentNode.getYesId());
                    mQuestion.setText(mCurrentNode.getQuestion());
                }
            }
        });
        mNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentNode.getYesId() == -1) {
                    // the only reason the no id would be -1 is if it's a "is it a moose?" kind of question.
                    // we don't know this animal...
                    mQuestionLayout.setVisibility(View.GONE);
                    mNewAnimalLayout.setVisibility(View.VISIBLE);
                    // enable edit text
                    mNewAnimalEditText.setEnabled(true);
                    mNewAnimalEditText.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                } else {
                    // move down the yes line
                    mCurrentNode = mDb.getRow(mCurrentNode.getNoId());
                    mQuestion.setText(mCurrentNode.getQuestion());
                }
            }
        });
        mSubmitNewAnimal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mNewAnimalEditText.getText().toString().trim().equals("")) {
                    String lastAnimal = mCurrentNode.getAnimal();

                    // hide new animal layout, disable edit text
                    mNewAnimalLayout.setVisibility(View.GONE);
                    mNewAnimalEditText.setEnabled(false);

                    // logic to know when to use "a" vs "an"
                    String newAnimalArticle;
                    String lastAnimalArticle;

                    if (startsWithVowel(mNewAnimalEditText.getText().toString())) {
                        newAnimalArticle = AN;
                    } else {
                        newAnimalArticle = A;
                    }

                    if (startsWithVowel(lastAnimal)) {
                        lastAnimalArticle = AN;
                    } else {
                        lastAnimalArticle = A;
                    }

                    // set new question prompt
                    mNewQuestionPrompt.setText(String.format(getResources().getString(R.string.prompt_new_question), newAnimalArticle, mNewAnimalEditText.getText().toString().toLowerCase().trim(), lastAnimalArticle, lastAnimal));

                    // show new question layout
                    mNewQuestionLayout.setVisibility(View.VISIBLE);
                    mNewQuestionEditText.setEnabled(true);
                    mNewQuestionEditText.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                }
            }
        });
        mSubmitNewQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mNewQuestionEditText.getText().toString().trim().equals("")) {

                    // if the user didn't add "?", add one here.
                    if (!mNewQuestionEditText.getText().toString().trim().endsWith("?")) {
                        mNewQuestionEditText.setText(mNewQuestionEditText.getText().toString().trim().concat("?"));
                    }

                    // hide new question layout, disable edit text
                    mNewQuestionLayout.setVisibility(View.GONE);
                    mNewQuestionEditText.setEnabled(false);

                    // logic to know when to use "a" vs "an"
                    String newAnimalArticle;

                    if (startsWithVowel(mNewAnimalEditText.getText().toString())) {
                        newAnimalArticle = AN;
                    } else {
                        newAnimalArticle = A;
                    }

                    // set yes/no question prompt
                    mQuestionYesNoPrompt.setText(String.format(getResources().getString(R.string.prompt_question_yes_no), newAnimalArticle, mNewAnimalEditText.getText().toString().toLowerCase().trim(), mNewQuestionEditText.getText().toString().trim()));
                    // show yes/no question layout
                    mQuestionYesNoLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        mQuestionYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // insert node YES

                // make node for new animal
                String newAnimal = mNewAnimalEditText.getText().toString().trim();

                String newAnimalArticle;

                if (startsWithVowel(newAnimal)) {
                    newAnimalArticle = AN;
                } else {
                    newAnimalArticle = A;
                }

                Node yesNode = new Node(mDb.getRowCount(), String.format(getResources().getString(R.string.guess_animal), newAnimalArticle, newAnimal), newAnimal);

                // make node for old animal (add one to row count, because we haven't added the yesNode row yet)
                Node noNode = new Node(mDb.getRowCount() + 1, mCurrentNode.getQuestion(), mCurrentNode.getAnimal());

                // set new question to current node
                mCurrentNode.setQuestion(mNewQuestionEditText.getText().toString().trim());

                // get rid of animal, since it's no longer a "is it a frog?" type question
                mCurrentNode.setAnimal(null);

                // set new yes/no children to current node
                mCurrentNode.setYesId(yesNode.getId());
                mCurrentNode.setNoId(noNode.getId());

                // update database
                mDb.addRow(yesNode);
                mDb.addRow(noNode);
                mDb.updateRow(mCurrentNode);

                // hide yes/no question layout
                mQuestionYesNoLayout.setVisibility(View.GONE);
                // show play again layout with "I know a new animal" prompt
                mPlayAgainPrompt.setText(getResources().getString(R.string.prompt_play_again_new_animal));
                mPlayAgainLayout.setVisibility(View.VISIBLE);
            }
        });
        mQuestionNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // insert node NO

                // make node for new animal
                String newAnimal = mNewAnimalEditText.getText().toString().trim();

                String newAnimalArticle;

                if (startsWithVowel(newAnimal)) {
                    newAnimalArticle = AN;
                } else {
                    newAnimalArticle = A;
                }

                Node noNode = new Node(mDb.getRowCount(), String.format(getResources().getString(R.string.guess_animal), newAnimalArticle, newAnimal), newAnimal);

                // make node for old animal (add one to row count, because we haven't added the noNode row yet)
                Node yesNode = new Node(mDb.getRowCount() + 1, mCurrentNode.getQuestion(), mCurrentNode.getAnimal());

                // set new question to current node
                mCurrentNode.setQuestion(mNewQuestionEditText.getText().toString());

                // get rid of animal, since it's no longer a "is it a frog?" type question
                mCurrentNode.setAnimal(null);

                // set new yes/no children to current node
                mCurrentNode.setYesId(yesNode.getId());
                mCurrentNode.setNoId(noNode.getId());

                // update database
                mDb.addRow(yesNode);
                mDb.addRow(noNode);
                mDb.updateRow(mCurrentNode);

                // hide yes/no question layout
                mQuestionYesNoLayout.setVisibility(View.GONE);
                // show play again layout with "I know a new animal" prompt
                mPlayAgainPrompt.setText(getResources().getString(R.string.prompt_play_again_new_animal));
                mPlayAgainLayout.setVisibility(View.VISIBLE);
            }
        });
        mPlayAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // clear edit texts

                mNewAnimalEditText.setText("");
                mNewQuestionEditText.setText("");

                // show start game layout
                mPlayAgainLayout.setVisibility(View.GONE);
                mStartGameLayout.setVisibility(View.VISIBLE);

                // start back at the top of the question tree.
                mCurrentNode = mDb.getRow(0);
            }
        });
        mQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // quit
                getActivity().finish();
            }
        });
    }

    private Boolean startsWithVowel(String animal) {
        return animal.startsWith("a") || animal.startsWith("e") || animal.startsWith("i") || animal.startsWith("o") || animal.startsWith("u");
    }

    public class DBHandler extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "DATABASE_QUESTIONS";
        private static final String TABLE_QUESTIONS = "TABLE_QUESTIONS";
        private static final String KEY_ID = "KEY_ID";
        private static final String KEY_QUESTION = "KEY_QUESTION";
        private static final String KEY_ANIMAL = "KEY_ANIMAL";
        private static final String KEY_YES_ID = "KEY_YES_ID";
        private static final String KEY_NO_ID = "KEY_NO_ID";


        public DBHandler(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            String CREATE_TABLE = "CREATE TABLE " + TABLE_QUESTIONS
                    + "("
                    + KEY_ID + " INTEGER PRIMARY KEY,"
                    + KEY_QUESTION + " TEXT,"
                    + KEY_ANIMAL + " TEXT,"
                    + KEY_YES_ID + " INTEGER,"
                    + KEY_NO_ID + " INTEGER"
                    + ")";
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Drop older table if existed
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTIONS);
            // Creating tables again
            onCreate(db);
        }

        public void addRow(Node node) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(KEY_ID, node.getId());
            values.put(KEY_QUESTION, node.getQuestion());
            values.put(KEY_ANIMAL, node.getAnimal());
            values.put(KEY_YES_ID, node.getYesId());
            values.put(KEY_NO_ID, node.getNoId());

            // Insert Row
            db.insert(TABLE_QUESTIONS, null, values);
            db.close();
        }

        public Node getRow(int id) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_QUESTIONS, new String[] { KEY_ID, KEY_QUESTION, KEY_ANIMAL, KEY_YES_ID, KEY_NO_ID }, KEY_ID + "=?", new String[] { String.valueOf(id) },
                    null, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();
            }

            // create a new node with all the data
            Node  node = new Node (
                    cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                    cursor.getString(cursor.getColumnIndex(KEY_QUESTION)),
                    cursor.getString(cursor.getColumnIndex(KEY_ANIMAL)),
                    cursor.getInt(cursor.getColumnIndex(KEY_YES_ID)),
                    cursor.getInt(cursor.getColumnIndex(KEY_NO_ID))
            );

            cursor.close();

            return node;
        }

        public int updateRow(Node node) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_QUESTION, node.getQuestion());
            values.put(KEY_ANIMAL, node.getAnimal());
            values.put(KEY_YES_ID, node.getYesId());
            values.put(KEY_NO_ID, node.getNoId());

            return db.update(TABLE_QUESTIONS, values, KEY_ID + " = ?", new String[]{String.valueOf(node.getId())});
        }

        public int getRowCount() {
            String countQuery = "SELECT * FROM " + TABLE_QUESTIONS;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(countQuery, null);
            int count = cursor.getCount();
            cursor.close();
            return count;
        }
    }
}

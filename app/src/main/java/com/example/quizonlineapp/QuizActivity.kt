package com.example.quizonlineapp

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.quizonlineapp.databinding.ActivityQuizBinding
import com.example.quizonlineapp.databinding.ScoreDialogBinding
import np.com.bimalkafle.quizonline.QuestionModel

class QuizActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        var questionModelList: List<QuestionModel> = listOf()
        var time: String = ""
    }

    private lateinit var binding: ActivityQuizBinding
    private var currentQuestionIndex = 0
    private var selectedAnswer = ""
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btn0.setOnClickListener(this@QuizActivity)
            btn1.setOnClickListener(this@QuizActivity)
            btn2.setOnClickListener(this@QuizActivity)
            btn3.setOnClickListener(this@QuizActivity)
            nextBtn.setOnClickListener(this@QuizActivity)
        }

        if (questionModelList.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "No questions available or time not set!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        loadQuestions()
        startTimer()
    }

    private fun startTimer() {
        val totalTimeInMillis = time.toInt() * 60 * 1000L
        object : CountDownTimer(totalTimeInMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                binding.timerIndicatorTextview.text = String.format("%02d:%02d", minutes, remainingSeconds)
            }

            override fun onFinish() {
                Toast.makeText(this@QuizActivity, "Time's up!", Toast.LENGTH_LONG).show()
                finish() // Ends the quiz when the timer finishes
            }
        }.start()
    }

    private fun loadQuestions() {
        if (currentQuestionIndex == questionModelList.size) {
            finishQuiz()
            return
        }

        // Reset selected answer for the next question.
        selectedAnswer = ""

        if (questionModelList.isEmpty()) {
            Toast.makeText(this, "No questions to display!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding.apply {
            val currentQuestion = questionModelList[currentQuestionIndex]
            questionIndicatorTextview.text = "Question ${currentQuestionIndex + 1} / ${questionModelList.size}"
            questionProgressIndicator.progress =
                ((currentQuestionIndex.toFloat() / questionModelList.size.toFloat()) * 100).toInt()
            questionTextview.text = currentQuestion.question
            btn0.text = currentQuestion.options[0]
            btn1.text = currentQuestion.options[1]
            btn2.text = currentQuestion.options[2]
            btn3.text = currentQuestion.options[3]

            // Reset button colors
            btn0.setBackgroundColor(getColor(R.color.gray))
            btn1.setBackgroundColor(getColor(R.color.gray))
            btn2.setBackgroundColor(getColor(R.color.gray))
            btn3.setBackgroundColor(getColor(R.color.gray))
        }
    }

    override fun onClick(view: View?) {
        val clickedBtn = view as Button

        if (clickedBtn.id == R.id.next_btn) {
            // Check if an answer has been selected.
            if (selectedAnswer.isEmpty()) {
                Toast.makeText(this, "Please select an answer to continue", Toast.LENGTH_SHORT).show()
                return
            }

            // Validate the answer.
            if (selectedAnswer == questionModelList[currentQuestionIndex].correct) {
                score++
                Log.i("Quiz Score", "Current Score: $score")
            }

            // Move to the next question.
            currentQuestionIndex++
            loadQuestions()
        } else {
            // An option button is clicked.
            selectedAnswer = clickedBtn.text.toString()

            // Highlight the selected answer.
            binding.apply {
                btn0.setBackgroundColor(getColor(R.color.gray))
                btn1.setBackgroundColor(getColor(R.color.gray))
                btn2.setBackgroundColor(getColor(R.color.gray))
                btn3.setBackgroundColor(getColor(R.color.gray))
            }
            clickedBtn.setBackgroundColor(getColor(R.color.orange))
        }
    }

    private fun finishQuiz() {
        val totalQuestions = questionModelList.size
        val percentage = ((score.toFloat() / totalQuestions.toFloat()) * 100).toInt()

        val dialogBinding = ScoreDialogBinding.inflate(layoutInflater)
        dialogBinding.apply {
            scoreProgressIndicator.progress = percentage
            scoreProgressText.text = "$percentage %"
            if (percentage > 60) {
                scoreTitle.text = "Congrats! You have passed"
                scoreTitle.setTextColor(Color.BLUE)
            } else {
                scoreTitle.text = "Oops! You have failed"
                scoreTitle.setTextColor(Color.RED)
            }
            scoreSubtitle.text = "$score out of $totalQuestions are correct"
            finishBtn.setOnClickListener {
                finish()
            }
        }

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .show()
    }
}

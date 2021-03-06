package tuk.myprivateapp.miniproject

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import tuk.myprivateapp.miniproject.memo.MemoActivity
import tuk.myprivateapp.miniproject.memo.ShowMemoActivity
import tuk.myprivateapp.miniproject.memo.ShowMemoListActivity
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity(), CalendarAdapter.OnItemListener {

    lateinit var monthYearText: TextView
    lateinit var calendarRecyclerView: RecyclerView
    lateinit var selectedDate: LocalDate
    lateinit var fav_btn : FloatingActionButton

    val existFileArr: ArrayList<String> = ArrayList()

    val SP_NAME = "memo_sp_storage"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initWidgets()
        selectedDate = LocalDate.now()
        setMonthView()

        fav_btn.setOnClickListener {
            Toast.makeText(this, "플로팅 버튼 클릭", Toast.LENGTH_SHORT).show()
            val intentToMemoList = Intent(applicationContext, ShowMemoListActivity::class.java)
            startActivity(intentToMemoList)
        }


    }


    private fun initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView)
        monthYearText = findViewById(R.id.monthYearTV)
        fav_btn = findViewById(R.id.fav_btn)
    }

    //뷰에 뿌려주는 함수
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setMonthView() {
        monthYearText.setText(monthYearFromDate(selectedDate))
        //월별 일수 가져오기 함수사용 일별 요일이 맞게 설정하는 함수임 그걸 배열에 넣어서 리턴함
        val daysInMonth = daysInMonthArray(selectedDate)
        //캘린더 어뎁터에 월별 맞춤 요일 배열과 이 함수를 넘겨준다 . this는 왜 있지 ? 리스너 부여?
        val calendarAdapter = CalendarAdapter(daysInMonth, this)
        //RecyclerView의 아이템의 배치와 재사용에 대한 정책을 결정하면 LayoutManager의 종류에 따라 아이템의 배치가 변경됩니다
        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(applicationContext, 7)
        calendarRecyclerView.setLayoutManager(layoutManager)
        calendarRecyclerView.setAdapter(calendarAdapter)
    }

    //리사이클러뷰에 월별로 일수 맞게 설정하면ㅅ 셀 설정하는 것
    @RequiresApi(Build.VERSION_CODES.O)
    private fun daysInMonthArray(date: LocalDate): ArrayList<String> {
        val daysInMonthArray: ArrayList<String> = ArrayList()
        val yearMonth: YearMonth = YearMonth.from(date)
        val daysInMonth: Int = yearMonth.lengthOfMonth()
        val firstOfMonth: LocalDate = selectedDate.withDayOfMonth(1)
        val dayOfWeek = firstOfMonth.dayOfWeek.value
        monthYearFromDate(selectedDate)
        for (i in 1..42) {
            var confrFile = (monthYearFromDate(selectedDate) + "_"
                    + i.toString() + ".txt")

            var SDpath = Environment.getExternalStorageDirectory().absolutePath
            var file1 = File(SDpath + "/myDiary/" + confrFile)

            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add("")
            } else {
                if (file1.exists()) {
                    existFileArr.add(file1.toString())

                    daysInMonthArray.add((i - dayOfWeek).toString())
                } else {
                    daysInMonthArray.add((i - dayOfWeek).toString())
                }
            }


        }
        return daysInMonthArray
    }

    //보여질 년도와 월
    @RequiresApi(Build.VERSION_CODES.O)
    private fun monthYearFromDate(date: LocalDate): String {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        return date.format(formatter)
    }




    //밑에 두 함수는 버튼 함
    @RequiresApi(Build.VERSION_CODES.O)
    fun previousMonthAction(view: View?) {
        selectedDate = selectedDate.minusMonths(1)
        setMonthView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun nextMonthAction(view: View?) {
        selectedDate = selectedDate.plusMonths(1)
        setMonthView()
    }




    // daytext가 널이 아니면 선택 메시지를 날림
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemClick(position: Int, dayText: String?) { //position 은 달력 내의 인덱스를 말함

        var fileName : String

        fileName = (monthYearFromDate(selectedDate) + "_"
                + dayText.toString() + ".txt")

        var strSDpath = Environment.getExternalStorageDirectory().absolutePath


        var file1 = File(strSDpath + "/myDiary/" + fileName)

        //파일 있으면 내용 보여줌
        if(file1.exists()){

            var str_context : String
            val intentToShowMemo = Intent(applicationContext, ShowMemoActivity::class.java)

            try {
                var inFs = FileInputStream(file1)
                var txt = ByteArray(inFs.available())
                inFs.read(txt)
                str_context = txt.toString(Charsets.UTF_8).trim()
                //메모 내용 넘기기 후에 이미지나 다른 정보들 클래스로 따로 빼서 구조체 만든 다음에 넘기면 될듯?
                intentToShowMemo.putExtra("memocontext_text", str_context) /*송신*/
                intentToShowMemo.putExtra("day", dayText.toString()) /*송신*/
                intentToShowMemo.putExtra("Myear", monthYearFromDate(selectedDate).toString())
                inFs.close()
            } catch (e : IOException) {
                Toast.makeText(this, "error",Toast.LENGTH_SHORT).show()
            }

            //해쉬맵으로 키는 파일이름 밸류는 기분아이디로 설정해서 값 받아온다음에 키, 즉 파일에 해당하는 기분 정보를 넘겨줌
            //if 이 파일의 기분이 222112313 이면 해당 이미지 출력
            //Toast.makeText(this, t_hashMap.get(fileName).toString() ,Toast.LENGTH_SHORT).show() //제대로 들어오는지 확인
            intentToShowMemo.putExtra("stateID", readSharedPreference(fileName)) /*송신*/

            startActivity(intentToShowMemo)
        }
        else{
            val message = "Selected Date " + dayText + " " + monthYearFromDate(selectedDate)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            val intentToMemo = Intent(applicationContext, MemoActivity::class.java)

            //인텐트 스택 삭제
            //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            intentToMemo.putExtra("day", dayText.toString()) /*송신*/
            intentToMemo.putExtra("Myear", monthYearFromDate(selectedDate).toString())

            startActivity(intentToMemo)
        }


    }


    fun readSharedPreference(key:String):String{
        val sp = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        return sp.getString(key,"")?:""
    }

}
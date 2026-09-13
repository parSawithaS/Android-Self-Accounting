package ir.androidir.SelfAccounting.ui.backupPage

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.DialogRestoreBackupBinding
import ir.androidir.SelfAccounting.databinding.FragmentCreateBackupBinding
import ir.androidir.SelfAccounting.model.HesabchiDataBase
import ir.androidir.SelfAccounting.model.dao.BudgetDao
import ir.androidir.SelfAccounting.model.dao.CardDao
import ir.androidir.SelfAccounting.model.dao.CategoryDao
import ir.androidir.SelfAccounting.model.dao.DebtDao
import ir.androidir.SelfAccounting.model.dao.TransactionDao
import ir.androidir.SelfAccounting.model.dataClasses.nonDataBase.BackupFile
import ir.androidir.SelfAccounting.ui.homeScreen.MainActivity
import ir.androidir.SelfAccounting.utils.date.DateUtils.getDate
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class BackupBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentCreateBackupBinding
    private val gson = Gson()
    private lateinit var budgetDao: BudgetDao
    private lateinit var cardDao: CardDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var debtDao: DebtDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var backupFile: BackupFile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateBackupBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hesabchiDataBase = HesabchiDataBase.getDataBase(requireContext())!!
        Thread {
            budgetDao = hesabchiDataBase.budgetDao
            cardDao = hesabchiDataBase.cardDao
            categoryDao = hesabchiDataBase.categoryDao
            debtDao = hesabchiDataBase.debtDao
            transactionDao = hesabchiDataBase.transactionDao
        }.start()



        binding.btnCreateBackup.setOnClickListener {
            createBackup()
        }
        binding.btnOpenBackup.setOnClickListener {
            openFile()
        }
    }

    private fun createBackup() {
        val budgetData = budgetDao.selectData()
        val cardData = cardDao.selectData()
        val categoryData = categoryDao.selectData()
        val debtData = debtDao.selectData()
        val transactionData = transactionDao.selectData()
        backupFile = BackupFile(budgetData, cardData, categoryData, debtData, transactionData)

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            val date = getDate(requireContext(),1)
            putExtra(Intent.EXTRA_TITLE, "HesabchiBackup${date[2]+date[1]+date[0]}.json")
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        createFile.launch(intent)
    }

    private val createFile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data!!.data.let { uri ->
                val jsonFile = gson.toJson(backupFile)
                val outputStream = requireContext().contentResolver.openOutputStream(uri!!)!!
                val fileWriter = BufferedWriter(OutputStreamWriter(outputStream))
                fileWriter.write(jsonFile)
                fileWriter.close()

                dismiss()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.backup_created),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        getBackupFile.launch(intent)
    }

    private val getBackupFile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data!!.data.let { uri ->
                val dialog = AlertDialog.Builder(requireContext()).create()
                val dialogBinding = DialogRestoreBackupBinding.inflate(layoutInflater)

                dialog.setView(dialogBinding.root)
                dialog.show()

                dialogBinding.txtYes2.setOnClickListener {
                    dialog.dismiss()
                    dismiss()

                    val inputStream =
                        requireContext().contentResolver.openInputStream(uri!!)!!
                    val fileReader = BufferedReader(InputStreamReader(inputStream))
                    val txt = fileReader.readText()
                    val backupData = gson.fromJson(txt, BackupFile::class.java)

                    restoreBackup(backupData)
                }
                dialogBinding.txtNo2.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }
    }

    private fun restoreBackup(data : BackupFile) {
        budgetDao.deleteAll()
        cardDao.deleteAll()
        categoryDao.deleteAll()
        debtDao.deleteAll()
        transactionDao.deleteAll()

        budgetDao.insertAll(data.BudgetData)
        cardDao.insertAll(data.CardData)
        categoryDao.insertAll(data.CategoryData)
        debtDao.insertAll(data.DebtData)
        transactionDao.insertAll(data.TransactionData)

        Toast.makeText(requireContext(), getString(R.string.backup_restored), Toast.LENGTH_SHORT)
            .show()
        val intent = Intent(requireContext() , MainActivity::class.java)
        startActivity(intent)
    }

}
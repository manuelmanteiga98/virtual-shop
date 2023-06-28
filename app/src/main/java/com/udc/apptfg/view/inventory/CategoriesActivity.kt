package com.udc.apptfg.view.inventory

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.udc.apptfg.R
import com.udc.apptfg.databinding.ActivityCategoriesBinding
import com.udc.apptfg.viewmodel.inventory.CategoryViewModel

class CategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoriesBinding
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.title = getString(R.string.manage_categories)

        // List View preparation
        var arrayAdapter: ArrayAdapter<*>
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList<String>())
        binding.categoriesListView.adapter = arrayAdapter

        // Add category button
        binding.categoriesAddButton.setOnClickListener {
            showAddCategoryDialog()
        }

        // Delete logic
        binding.categoriesListView.setOnItemLongClickListener { _, _, i, _ ->
            val category = categoryViewModel.categories.value?.get(i).toString()
            if (category.isNotBlank()) deleteCategory(category)
            true
        }

        // Observes category list
        categoryViewModel.categories.observe(this) { categories ->
            arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
            binding.categoriesListView.adapter = arrayAdapter
        }

        // init list of categories
        categoryViewModel.getAllCategories()

    }

    private fun showAddCategoryDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_category, null)
        dialogBuilder.setView(view)
        val dialog = dialogBuilder.create()

        view.findViewById<Button>(R.id.categoryDialogAdd).setOnClickListener {
            val name = view.findViewById<EditText>(R.id.categoryDialogName).text.toString()
            if (name.isNotBlank()) addCategory(name)
            dialog.dismiss()
        }

        view.findViewById<Button>(R.id.categoryDialogCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deleteCategory(name: String) {
        if (auth.currentUser != null) {
            val email = auth.currentUser!!.email.toString()
            val itemRef = db.collection("/users").document(email)
                .collection("/items")
            itemRef.whereEqualTo("category", name).get().addOnSuccessListener { items ->
                if (items.isEmpty) showDeleteCategoryDialog(name)
                else {
                    notEmptyCategoryDialog()
                }
            }
        }
    }

    private fun showDeleteCategoryDialog(name: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.warning))
        builder.setMessage(getString(R.string.category_delete_dialog) + " " + name)
        builder.setPositiveButton(getString(R.string.accept)) { _: DialogInterface, _: Int ->
            categoryViewModel.delCategory(name)
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun notEmptyCategoryDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.warning))
        builder.setMessage(getString(R.string.category_not_empty))
        builder.setPositiveButton(getString(R.string.accept)) { _: DialogInterface, _: Int ->
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun addCategory(name: String) {
        categoryViewModel.addCategory(name)
    }

}
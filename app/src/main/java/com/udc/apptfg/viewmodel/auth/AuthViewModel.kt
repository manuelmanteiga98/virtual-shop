package com.udc.apptfg.viewmodel.auth

import android.graphics.Bitmap
import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.udc.apptfg.model.auth.AuthModel
import com.udc.apptfg.model.ErrorModel
import com.udc.apptfg.util.Util
import java.io.ByteArrayOutputStream


class AuthViewModel : ViewModel() {
    var authModel = MutableLiveData<AuthModel>()
    var errorModel = MutableLiveData<ErrorModel>()
    var reset = MutableLiveData<Boolean>()
    var manager = MutableLiveData<Boolean>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun login(email: String, password: String) {
        val isValidEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        if (isValidEmail && password.isNotBlank()) {
            auth.signInWithEmailAndPassword(
                email,
                password
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    authModel.postValue(AuthModel(email))
                } else {
                    errorModel.postValue(ErrorModel("firebase", it.exception?.message.toString()))
                }
            }
        } else {
            handleErrorsLogin(password, isValidEmail)
        }
    }

    fun forgot(email: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorModel.postValue(ErrorModel("email_validation"))
        } else {
            auth.sendPasswordResetEmail(email)
                .addOnFailureListener {
                    errorModel.postValue(ErrorModel("firebase", it.message.toString()))
                }
                .addOnSuccessListener {
                    reset.postValue(true)
                }
        }
    }

    private fun handleErrorsLogin(password: String, isValidEmail: Boolean) {
        if (!isValidEmail) {
            errorModel.postValue(ErrorModel("email_validation"))
        } else if (password.isBlank()) {
            errorModel.postValue(ErrorModel("password_blank"))
        } else errorModel.postValue(ErrorModel("unexpected"))
    }

    fun register(
        email: String,
        password: String,
        password2: String,
        name: String,
        surname: String,
        jobType: Int,
        mgrEmail: String,
        photo: Bitmap?
    ) {
        val isValidEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        var isValidEmailMGR = true
        if (jobType == 1) isValidEmailMGR = Patterns.EMAIL_ADDRESS.matcher(mgrEmail).matches()
        if (password == password2
            && isValidEmail
            && password.isNotBlank()
            && name.isNotBlank()
            && surname.isNotBlank()
            && ((jobType == 0) || isValidEmailMGR)
        ) {
            //check email already exist or not.
            if (jobType == 1) {
                db.collection("users").document(mgrEmail).get()
                    .addOnFailureListener {
                        errorModel.postValue(ErrorModel("mgr_not_found"))
                    }
                    .addOnSuccessListener {
                        val jobMgr = it.getLong("jobtype")?.toInt()
                        if ((jobMgr != null) && (jobMgr == 0)) {
                            createUser(email, password, name, surname, jobType, mgrEmail, photo)
                        } else {
                            errorModel.postValue(ErrorModel("not_mgr"))
                        }
                    }
            } else {
                createUser(email, password, name, surname, jobType, "", photo)
            }
        } else {
            handleErrorsRegister(password, password2, isValidEmail, name, surname, isValidEmailMGR)
        }
    }

    private fun handleErrorsRegister(
        password: String, password2: String, isValidEmail: Boolean,
        name: String, surname: String, isValidEmailMGR: Boolean
    ) {
        if (!isValidEmail) {
            errorModel.postValue(ErrorModel("email_validation"))
        } else if (password.isBlank()) {
            errorModel.postValue(ErrorModel("password_blank"))
        } else if (password != password2) {
            errorModel.postValue(ErrorModel("password_match"))
        } else if (name.isBlank()) errorModel.postValue(ErrorModel("name_blank"))
        else if (surname.isBlank()) errorModel.postValue(ErrorModel("surname_blank"))
        else if (!isValidEmailMGR) errorModel.postValue(ErrorModel("mgr_validation"))
        else errorModel.postValue(ErrorModel("unexpected"))
    }

    private fun createUser(
        email: String,
        password: String,
        name: String,
        surname: String,
        jobType: Int,
        mgrEmail: String,
        photo: Bitmap?
    ) {
        auth.createUserWithEmailAndPassword(
            email,
            password
        ).addOnCompleteListener {
            if (it.isSuccessful) {
                authModel.postValue(AuthModel(email))
                val regDate = Util.getTodayDate()
                db.collection("users").document(email).set(
                    hashMapOf(
                        "name" to name,
                        "surname" to surname,
                        "jobtype" to jobType,
                        "mgr_email" to mgrEmail,
                        "registration_date" to regDate
                    )
                ).addOnSuccessListener {
                    if (photo != null) {
                        val storageRef = storage.reference
                        val pathRef = storageRef.child(auth.currentUser!!.email.toString())
                        val itemRef = pathRef.child(email)
                        val stream = ByteArrayOutputStream()
                        photo.compress(Bitmap.CompressFormat.JPEG, 10, stream)
                        itemRef.putBytes(stream.toByteArray())
                    }
                }
            } else {
                errorModel.postValue(ErrorModel("firebase", it.exception?.message.toString()))
            }
        }
    }

    fun isManager(id:String){
        db.collection("users").document(id).get().addOnSuccessListener {  user->
            val jobType = user.getLong("jobtype")?.toInt()
            manager.postValue(jobType==0)
        }
    }

}
#include "zinniajni.h"
#include "zinnia/zinnia/zinnia.h"

static jfieldID recognizer_id_field = NULL;
static jfieldID character_id_field = NULL;
static jfieldID result_set_id_field = NULL;
static jclass result_set_cls = NULL;
static jmethodID result_set_constructor_method = NULL;

zinnia_recognizer_t *extract_recognizer_id
        (JNIEnv *env, jobject obj)
{
    jlong recognizer_id = env->GetLongField(obj, recognizer_id_field);
    zinnia_recognizer_t *p = reinterpret_cast<zinnia_recognizer_t *>(recognizer_id);
    return p;
}

zinnia_character_t *extract_character_id
        (JNIEnv *env, jobject obj)
{
    jlong character_id = env->GetLongField(obj, character_id_field);
    zinnia_character_t *p = reinterpret_cast<zinnia_character_t *>(character_id);
    return p;
}

zinnia_result_t *extract_result_id
        (JNIEnv *env, jobject obj)
{
    jlong result_id = env->GetLongField(obj, result_set_id_field);
    zinnia_result_t *p = reinterpret_cast<zinnia_result_t *>(result_id);
    return p;
}

/*
 * Class:     me_rotatingticket_yajd_util_zinnia_Recognizer
 * Method:    nativeInit
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_me_rotatingticket_yajd_util_zinnia_Recognizer_nativeInit
        (JNIEnv *env, jclass cls)
{
    recognizer_id_field = env->GetFieldID(cls, "id", "J");
}

/*
 * Class:     me_rotatingticket_yajd_util_zinnia_Recognizer
 * Method:    create
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_me_rotatingticket_yajd_util_zinnia_Recognizer_create
        (JNIEnv *, jclass cls)
{
    zinnia_recognizer_t *p = zinnia_recognizer_new();
    return reinterpret_cast<jlong>(p);
}

/*
 * Class:     me_rotatingticket_yajd_util_zinnia_Recognizer
 * Method:    open
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_me_rotatingticket_yajd_util_zinnia_Recognizer_open
        (JNIEnv *env, jobject obj, jstring model_path)
{
    zinnia_recognizer_open(extract_recognizer_id(env, obj), env->GetStringUTFChars(model_path, NULL));
}

/*
 * Class:     me_rotatingticket_yajd_util_zinnia_Recognizer
 * Method:    classify
 * Signature: (Lme/rotatingticket/yajd/util/zinnia/Character;J)Lme/rotatingticket/yajd/util/zinnia/ResultSet;
 */
JNIEXPORT jobject JNICALL Java_me_rotatingticket_yajd_util_zinnia_Recognizer_classify
        (JNIEnv *env, jobject obj, jobject character, jlong nbest)
{
    zinnia_result_t *result = zinnia_recognizer_classify(extract_recognizer_id(env, obj),
                                                         extract_character_id(env, character),
                                                         static_cast<size_t>(nbest));
    if (!result)
    {
        return 0;
    }
    else
    {
        return env->NewObject(result_set_cls,
                              result_set_constructor_method,
                              reinterpret_cast<jlong>(result));
    }
}

/*
 * Class:     me_rotatingticket_yajd_util_zinnia_Recognizer
 * Method:    what
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_me_rotatingticket_yajd_util_zinnia_Recognizer_what
        (JNIEnv *env, jobject obj)
{
    const char *result = zinnia_recognizer_strerror(extract_recognizer_id(env, obj));
    return env->NewStringUTF(result);
}

/*
 * Class:     me_rotatingticket_yajd_util_zinnia_Recognizer
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_me_rotatingticket_yajd_util_zinnia_Recognizer_close
        (JNIEnv *env, jobject obj)
{
    zinnia_recognizer_destroy(extract_recognizer_id(env, obj));
}

/*
 * Class:     me_rotatingticket_yajd_util_zinnia_ResultSet
 * Method:    nativeInit
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_me_rotatingticket_yajd_util_zinnia_ResultSet_nativeInit
        (JNIEnv *env, jclass cls)
{
    result_set_cls = reinterpret_cast<jclass>(env->NewGlobalRef(cls));
    result_set_constructor_method = env->GetMethodID(cls, "<init>", "(J)V");
    result_set_id_field = env->GetFieldID(cls, "id", "J");
}

/*
 * Class:     me_rotatingticket_yajd_util_zinnia_ResultSet
 * Method:    value
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_me_rotatingticket_yajd_util_zinnia_ResultSet_value
        (JNIEnv *env, jobject obj, jlong index)
{
    const char *value = zinnia_result_value(extract_result_id(env, obj),
                                            static_cast<size_t>(index));
    return env->NewStringUTF(value);
}

/*
 * Class:     me_rotatingticket_yajd_util_zinnia_ResultSet
 * Method:    score
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_me_rotatingticket_yajd_util_zinnia_ResultSet_score
        (JNIEnv *env, jobject obj, jlong index)
{
    float score = zinnia_result_score(extract_result_id(env, obj), static_cast<size_t>(index));
    return static_cast<jfloat>(score);
}

/*
 * Class:     me_rotatingticket_yajd_util_zinnia_ResultSet
 * Method:    size
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_me_rotatingticket_yajd_util_zinnia_ResultSet_size
        (JNIEnv *env, jobject obj)
{
    size_t size = zinnia_result_size(extract_result_id(env, obj));
    return static_cast<jlong>(size);
}

/*
 * Class:     me_rotatingticket_yajd_util_zinnia_ResultSet
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_me_rotatingticket_yajd_util_zinnia_ResultSet_close
        (JNIEnv *env, jobject obj)
{
    zinnia_result_destroy(extract_result_id(env, obj));
}

/*
 * Class:     me_rotatingticket_yajd_util_zinnia_Character
 * Method:    nativeInit
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_me_rotatingticket_yajd_util_zinnia_Character_nativeInit
        (JNIEnv *env, jclass cls)
{
    character_id_field = env->GetFieldID(cls, "id", "J");
}

/*
 * Class:     me_rotatingticket_yajd_util_zinnia_Character
 * Method:    create
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_me_rotatingticket_yajd_util_zinnia_Character_create
        (JNIEnv *, jclass)
{
    zinnia_character_t *p = zinnia_character_new();
    return reinterpret_cast<jlong>(p);
}

/*
 * Class:     me_rotatingticket_yajd_util_zinnia_Character
 * Method:    clear
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_me_rotatingticket_yajd_util_zinnia_Character_clear
        (JNIEnv *env, jobject obj)
{
    zinnia_character_clear(extract_character_id(env, obj));
}

/*
 * Class:     me_rotatingticket_yajd_util_zinnia_Character
 * Method:    setWidth
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_me_rotatingticket_yajd_util_zinnia_Character_setWidth
        (JNIEnv *env, jobject obj, jlong width)
{
    zinnia_character_set_width(extract_character_id(env, obj), static_cast<size_t>(width));
}

/*
 * Class:     me_rotatingticket_yajd_util_zinnia_Character
 * Method:    setHeight
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_me_rotatingticket_yajd_util_zinnia_Character_setHeight
        (JNIEnv *env, jobject obj, jlong height)
{
    zinnia_character_set_height(extract_character_id(env, obj), static_cast<size_t>(height));
}

/*
 * Class:     me_rotatingticket_yajd_util_zinnia_Character
 * Method:    draw
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_me_rotatingticket_yajd_util_zinnia_Character_draw
        (JNIEnv *env, jobject obj, jlong id, jint x, jint y)
{
    zinnia_character_add(extract_character_id(env, obj),
                         static_cast<size_t>(id),
                         static_cast<int>(x),
                         static_cast<int>(y));
}

/*
 * Class:     me_rotatingticket_yajd_util_zinnia_Character
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_me_rotatingticket_yajd_util_zinnia_Character_close
        (JNIEnv *env, jobject obj)
{
    zinnia_character_destroy(extract_character_id(env, obj));
}

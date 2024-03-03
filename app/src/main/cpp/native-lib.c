#include <error.h>
#include <proxy.h>
#include <params.h>
#include <packets.h>
#include <unistd.h>
#include <string.h>
#include <netdb.h>

#include <jni.h>
#include <android/log.h>

const enum demode DESYNC_METHODS[] = {
        DESYNC_NONE,
        DESYNC_SPLIT,
        DESYNC_DISORDER,
        DESYNC_FAKE,
        DESYNC_OOB,
};

extern int NOT_EXIT;

extern int get_default_ttl();

extern int get_addr(const char *str, struct sockaddr_ina *addr);

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, __attribute__((unused)) void *reserved) {
    oob_data.data = NULL;
    return JNI_VERSION_1_6;
}

JNIEXPORT jint JNICALL
Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniCreateSocket(
        JNIEnv *env,
        __attribute__((unused)) jobject thiz,
        jstring ip,
        jint port,
        jint max_connections,
        jint buffer_size,
        jint default_ttl,
        jboolean no_domain,
        jboolean desync_known,
        jint desync_method,
        jint split_position,
        jboolean split_at_host,
        jint fake_ttl,
        jstring fake_sni,
        jstring custom_oob_data,
        jboolean host_mixed_case,
        jboolean domain_mixed_case,
        jboolean host_remove_spaces,
        jboolean tls_record_split,
        jint tls_record_split_position,
        jboolean tls_record_split_at_sni) {

    struct sockaddr_ina s = {
            .in.sin_family = AF_INET,
            .in.sin_addr.s_addr = inet_addr("0.0.0.0"),
    };

    const char *address = (*env)->GetStringUTFChars(env, ip, 0);
    if (get_addr(address, &s) < 0) {
        return -1;
    }
    (*env)->ReleaseStringUTFChars(env, ip, address);

    s.in.sin_port = htons(port);

    params.max_open = max_connections;
    params.bfsize = buffer_size;
    params.def_ttl = default_ttl;
    params.resolve = !no_domain;
    params.de_known = desync_known;
    params.attack = DESYNC_METHODS[desync_method];
    params.split = split_position;
    params.split_host = split_at_host;
    params.ttl = fake_ttl;
    params.mod_http =
            MH_HMIX * host_mixed_case |
            MH_DMIX * domain_mixed_case |
            MH_SPACE * host_remove_spaces;
    params.tlsrec = tls_record_split;
    params.tlsrec_pos = tls_record_split_position;
    params.tlsrec_sni = tls_record_split_at_sni;

    if (!params.def_ttl && params.attack != DESYNC_NONE) {
        if ((params.def_ttl = get_default_ttl()) < 1) {
            return -1;
        }
    }

    int fd = listen_socket(&s);
    if (fd < 0) {
        uniperror("listen_socket");
        return get_e();
    }

    if (params.attack == DESYNC_FAKE) {
        const char *sni = (*env)->GetStringUTFChars(env, fake_sni, 0);
        LOG(LOG_S, "fake_sni: %s", sni);
        int res = change_tls_sni(sni, fake_tls.data, fake_tls.size);
        (*env)->ReleaseStringUTFChars(env, fake_sni, sni);
        if (res) {
            fprintf(stderr, "error chsni\n");
            return -1;
        }
    }

    if (params.attack == DESYNC_OOB) {
        const char *oob = (*env)->GetStringUTFChars(env, custom_oob_data, 0);
        const size_t oob_len = strlen(oob);
        LOG(LOG_L, "custom_oob_data: %s", oob);
        oob_data.size = oob_len;
        LOG(LOG_L, "before free");
        free(oob_data.data);
        LOG(LOG_L, "after free");
        oob_data.data = malloc(oob_len);
        if (oob_data.data == NULL) {
            uniperror("malloc");
            return -1;
        }
        memcpy(oob_data.data, oob, oob_len);
        (*env)->ReleaseStringUTFChars(env, custom_oob_data, oob);
    }

    LOG(LOG_S, "listen_socket, fd: %d", fd);
    return fd;
}

JNIEXPORT jint JNICALL
Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniStartProxy(
        __attribute__((unused)) JNIEnv *env,
        __attribute__((unused)) jobject thiz,
        jint fd) {
    LOG(LOG_S, "start_proxy, fd: %d", fd);
    NOT_EXIT = 1;
    if (event_loop(fd) < 0) {
        return get_e();
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniStopProxy(
        __attribute__((unused)) JNIEnv *env,
        __attribute__((unused)) jobject thiz,
        jint fd) {
    LOG(LOG_S, "stop_proxy, fd: %d", fd);
    if (shutdown(fd, SHUT_RDWR) < 0) {
        return get_e();
    }
    return 0;
}

#include <string.h>
#include <netdb.h>

#include <jni.h>
#include <android/log.h>

#include "byedpi/error.h"
#include "byedpi/proxy.h"
#include "byedpi/params.h"
#include "byedpi/packets.h"
#include "main.h"
#include "utils.h"

const enum demode DESYNC_METHODS[] = {
        DESYNC_NONE,
        DESYNC_SPLIT,
        DESYNC_DISORDER,
        DESYNC_FAKE,
        DESYNC_OOB,
};

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, __attribute__((unused)) void *reserved) {
    oob_data.data = NULL;
    default_params = params;
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
        jboolean custom_ttl,
        jboolean no_domain,
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

    struct sockaddr_ina s;

    const char *address = (*env)->GetStringUTFChars(env, ip, 0);
    int res = get_addr(address, &s);
    (*env)->ReleaseStringUTFChars(env, ip, address);
    if (res < 0) {
        uniperror("get_addr");
        return -1;
    }

    s.in.sin_port = htons(port);

    params.max_open = max_connections;
    params.bfsize = buffer_size;
    params.resolve = !no_domain;

    if (custom_ttl) {
        params.def_ttl = default_ttl;
        params.custom_ttl = 1;
    }

    if (!params.def_ttl) {
        if ((params.def_ttl = get_default_ttl()) < 1) {
            uniperror("get_default_ttl");
            reset_params();
            return -1;
        }
    }

    struct desync_params *dp = add(
            (void *) &params.dp,
            &params.dp_count,
            sizeof(struct desync_params)
    );
    if (!dp) {
        uniperror("add");
        reset_params();
        return -1;
    }

    dp->ttl = fake_ttl;
    dp->mod_http =
            MH_HMIX * host_mixed_case |
            MH_DMIX * domain_mixed_case |
            MH_SPACE * host_remove_spaces;

    struct part *part = add(
            (void *) &dp->parts,
            &dp->parts_n,
            sizeof(struct part)
    );
    if (!part) {
        uniperror("add");
        reset_params();
        return -1;
    }

    enum demode mode = DESYNC_METHODS[desync_method];

    part->flag = split_at_host ? OFFSET_SNI : 0;
    part->pos = split_position;
    part->m = mode;

    if (tls_record_split) {
        struct part *tlsrec_part = add(
                (void *) &dp->tlsrec,
                &dp->tlsrec_n,
                sizeof(struct part)
        );

        if (!tlsrec_part) {
            uniperror("add");
            reset_params();
            return -1;
        }

        tlsrec_part->flag = tls_record_split_at_sni ? OFFSET_SNI : 0;
        tlsrec_part->pos = tls_record_split_position;
    }

    if (mode == DESYNC_FAKE) {
        const char *sni = (*env)->GetStringUTFChars(env, fake_sni, 0);
        LOG(LOG_S, "fake_sni: %s", sni);
        res = change_tls_sni(sni, fake_tls.data, fake_tls.size);
        (*env)->ReleaseStringUTFChars(env, fake_sni, sni);
        if (res) {
            fprintf(stderr, "error chsni\n");
            return -1;
        }
    }

    if (mode == DESYNC_OOB) {
        const char *oob = (*env)->GetStringUTFChars(env, custom_oob_data, 0);
        const size_t oob_len = strlen(oob);

        oob_data.size = oob_len;
        oob_data.data = malloc(oob_len);
        if (oob_data.data == NULL) {
            uniperror("malloc");
            return -1;
        }
        memcpy(oob_data.data, oob, oob_len);
        (*env)->ReleaseStringUTFChars(env, custom_oob_data, oob);
    }

    params.mempool = mem_pool(0);
    if (!params.mempool) {
        uniperror("mem_pool");
        clear_params();
        return -1;
    }

    int fd = listen_socket(&s);
    if (fd < 0) {
        uniperror("listen_socket");
        return -1;
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
        uniperror("event_loop");
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

    int res = shutdown(fd, SHUT_RDWR);
    reset_params();

    if (res < 0) {
        uniperror("shutdown");
        return get_e();
    }
    return 0;
}

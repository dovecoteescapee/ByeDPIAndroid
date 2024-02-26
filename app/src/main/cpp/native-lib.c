#include <error.h>
#include <proxy.h>
#include <params.h>
#include <packets.h>
#include <unistd.h>
#include <string.h>
#include <netdb.h>
#include <sys/eventfd.h>

#include <jni.h>
#include <android/log.h>

const enum demode DESYNC_METHODS[] = {
        DESYNC_NONE,
        DESYNC_SPLIT,
        DESYNC_DISORDER,
        DESYNC_FAKE
};

extern struct packet fake_tls, fake_http;
extern int get_default_ttl();
extern int get_addr(const char *str, struct sockaddr_ina *addr);

JNIEXPORT jint JNICALL
Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniEventFd(JNIEnv *env, jobject thiz) {
    int fd = eventfd(0, EFD_NONBLOCK);
    if (fd < 0) {
        return -1;
    }
    return fd;
}

JNIEXPORT jint JNICALL
Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniStartProxy(
        JNIEnv *env,
        jobject thiz,
        jint event_fd,
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

    int res = listener(event_fd, s);

    if (close(event_fd) < 0) {
        uniperror("close");
    }

    return res < 0 ? get_e() : 0;
}

JNIEXPORT jint JNICALL
Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniStopProxy(JNIEnv *env, jobject thiz,
                                                                      jint event_fd) {
    if (eventfd_write(event_fd, 1) < 0) {
        uniperror("eventfd_write");
        LOG(LOG_S, "event_fd: %d", event_fd);
    }

    return 0;
}
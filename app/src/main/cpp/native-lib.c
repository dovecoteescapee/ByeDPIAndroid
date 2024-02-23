#include <error.h>
#include <proxy.h>
#include <params.h>
#include <packets.h>
#include <jni.h>
#include <android/log.h>
#include <unistd.h>
#include <pthread.h>
#include <string.h>

extern int NOT_EXIT;

struct packet fake_tls = {
        sizeof(tls_data), tls_data
},
        fake_http = {
        sizeof(http_data), http_data
};

struct params params = {
        .ttl = 8,
        .split = 3,
        .sfdelay = 3000,
        .attack = DESYNC_NONE,
        .split_host = 0,
        .def_ttl = 0,
        .custom_ttl = 0,
        .mod_http = 0,
        .tlsrec = 0,
        .tlsrec_pos = 0,
        .tlsrec_sni = 0,
        .de_known = 0,

        .ipv6 = 1,
        .resolve = 1,
        .max_open = 512,
        .bfsize = 16384,
        .baddr = {
                .sin6_family = AF_INET
        },
        .debug = 2
};

int get_default_ttl()
{
    int orig_ttl = -1, fd;
    socklen_t tsize = sizeof(orig_ttl);

    if ((fd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        uniperror("socket");
        return -1;
    }
    if (getsockopt(fd, IPPROTO_IP, IP_TTL,
                   (char *)&orig_ttl, &tsize) < 0) {
        uniperror("getsockopt IP_TTL");
    }
    close(fd);
    return orig_ttl;
}

void *run(void *srv) {
    LOG(LOG_S, "Start proxy thread");
    listener(*((struct sockaddr_ina *) srv));
    free(srv);
    LOG(LOG_S, "Stop proxy thread");
    return NULL;
}

JNIEXPORT jlong JNICALL
Java_io_github_dovecoteescapee_byedpi_ByeDpiVpnService_jniStartProxy(
        JNIEnv *env,
        jobject thiz,
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
        jboolean host_remove_space,
        jboolean tls_record_split,
        jint tls_record_split_position,
        jboolean tls_record_split_at_sni) {
    enum demode desync_methods[] = {DESYNC_NONE, DESYNC_SPLIT, DESYNC_DISORDER, DESYNC_FAKE};

    params.max_open = max_connections;
    params.bfsize = buffer_size;
    params.def_ttl = default_ttl;
    params.resolve = !no_domain;
    params.de_known = desync_known;
    params.attack = desync_methods[desync_method];
    params.split = split_position;
    params.split_host = split_at_host;
    params.ttl = fake_ttl;
    params.mod_http |= host_mixed_case ? MH_HMIX : 0;
    params.mod_http |= domain_mixed_case ? MH_DMIX : 0;
    params.mod_http |= host_remove_space ? MH_SPACE : 0;
    params.tlsrec = tls_record_split;
    params.tlsrec_pos = tls_record_split_position;
    params.tlsrec_sni = tls_record_split_at_sni;

    if (!params.def_ttl && params.attack != DESYNC_NONE) {
        if ((params.def_ttl = get_default_ttl()) < 1) {
            return -1;
        }
    }

    struct sockaddr_ina *srv = malloc(sizeof(struct sockaddr_ina));
    srv->in.sin_family = AF_INET;
    srv->in.sin_addr.s_addr = inet_addr("0.0.0.0");
    srv->in.sin_port = htons(port);

    NOT_EXIT = 1;

    pthread_t proxy_thread;
    if (pthread_create(&proxy_thread, NULL, run, srv) != 0) {
        LOG(LOG_S, "Failed to start proxy thread");
        return -1;
    }

    return proxy_thread;
}

JNIEXPORT void JNICALL
Java_io_github_dovecoteescapee_byedpi_ByeDpiVpnService_jniStopProxy(JNIEnv *env, jobject thiz, jlong proxy_thread) {
    NOT_EXIT = 0;
}

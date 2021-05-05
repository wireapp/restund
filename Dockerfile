FROM ubuntu:20.04
ARG extra_modules="zrest drain"
ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
    && apt-get install -y curl make gcc libssl-dev
RUN mkdir -p build && curl -sSLf https://github.com/creytiv/re/archive/v0.6.1.tar.gz  -o v0.6.1.tar.gz \
    && echo "cd5bfc79640411803b200c7531e4ba8a230da3806746d3bd2de970da2060fe43  v0.6.1.tar.gz" | sha256sum --check \
    && tar -C /build -xmvf v0.6.1.tar.gz

COPY . /build/restund
WORKDIR /build/restund

RUN make -C /build/re-0.6.1 RELEASE=1 EXTRA_CFLAGS="-std=gnu99" && make -C /build/re-0.6.1 PREFIX=/usr/local install && ldconfig
RUN make -C /build/restund RELEASE=1 EXTRA_CFLAGS="-std=gnu99" && make -C /build/restund PREFIX=/usr/local install
RUN apt-get remove -y make gcc && apt-get autoremove -y
RUN useradd --system --shell /bin/false -U restund
USER   restund
VOLUME /etc/restund.conf
VOLUME /etc/restund.auth
ENTRYPOINT [ "/usr/local/sbin/restund", "-n" ]

//package me.asu.net.message;
//
//
///**
// * ProtoMessage.
// * <code><pre>
// * format:
// *      4-cmdId,4-seqId,4-bodyLen,1-cmdType,1-bodyType,1-code,1-ttl, N-data.
// * header:
// *      lengthFieldOffset   = 8 pre header
// *      lengthFieldLength   = 4 only data length
// *      lengthAdjustment    = 4 after header
// *      initialBytesToStrip = 0
// * body:
// *      byte array.
// * </pre></code>
// * <p>2017 Suk All rights reserved.</p>
// *
// * 
// * @version 1.0.0
// * @since 2017-09-11 11:17
// */
//
//public interface IProtoMessage extends IMessage {
//
//    /**
//     * quick get cmdId.
//     *
//     * @return cmdId.
//     */
//    int cmdId();
//
//    /**
//     * quick get seqId.
//     *
//     * @return seqId.
//     */
//    int seqId();
//
//    /**
//     * quick get header.
//     *
//     * @return header
//     */
//    ProtoMessageHeader header();
//
//    /**
//     * get header.
//     *
//     * @return header.
//     */
//    ProtoMessageHeader getHeader();
//
//    /**
//     * set header.
//     *
//     * @param header ProtoMessageHeader
//     */
//    void setHeader(ProtoMessageHeader header);
//
//    /**
//     * quick get body.
//     *
//     * @return body.
//     */
//    byte[] body();
//
//    /**
//     * get body.
//     *
//     * @return body.
//     */
//    byte[] getBody();
//
//    /**
//     * set body
//     *
//     * @param body byte[]
//     */
//    void setBody(byte[] body);
//}
//

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: opensergo/proto/service_contract/v1/service_contract.proto

package io.opensergo.proto.service_contract.v1;

public interface FieldDescriptorOrBuilder extends
    // @@protoc_insertion_point(interface_extends:opensergo.proto.service_contract.v1.FieldDescriptor)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string name = 1;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <code>string name = 1;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>int32 number = 3;</code>
   * @return The number.
   */
  int getNumber();

  /**
   * <code>optional .opensergo.proto.service_contract.v1.FieldDescriptor.Label label = 4;</code>
   * @return Whether the label field is set.
   */
  boolean hasLabel();
  /**
   * <code>optional .opensergo.proto.service_contract.v1.FieldDescriptor.Label label = 4;</code>
   * @return The enum numeric value on the wire for label.
   */
  int getLabelValue();
  /**
   * <code>optional .opensergo.proto.service_contract.v1.FieldDescriptor.Label label = 4;</code>
   * @return The label.
   */
  io.opensergo.proto.service_contract.v1.FieldDescriptor.Label getLabel();

  /**
   * <pre>
   * If type_name is set, this need not be set.  If both this and type_name
   * are set, this must be one of TYPE_ENUM, TYPE_MESSAGE or TYPE_GROUP.
   * </pre>
   *
   * <code>.opensergo.proto.service_contract.v1.FieldDescriptor.Type type = 5;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <pre>
   * If type_name is set, this need not be set.  If both this and type_name
   * are set, this must be one of TYPE_ENUM, TYPE_MESSAGE or TYPE_GROUP.
   * </pre>
   *
   * <code>.opensergo.proto.service_contract.v1.FieldDescriptor.Type type = 5;</code>
   * @return The type.
   */
  io.opensergo.proto.service_contract.v1.FieldDescriptor.Type getType();

  /**
   * <pre>
   * For message and enum types, this is the name of the type.  If the name
   * starts with a '.', it is fully-qualified.  Otherwise, C++-like scoping
   * rules are used to find the type (i.e. first the nested types within this
   * message are searched, then within the parent, on up to the root
   * namespace).
   * </pre>
   *
   * <code>optional string type_name = 6;</code>
   * @return Whether the typeName field is set.
   */
  boolean hasTypeName();
  /**
   * <pre>
   * For message and enum types, this is the name of the type.  If the name
   * starts with a '.', it is fully-qualified.  Otherwise, C++-like scoping
   * rules are used to find the type (i.e. first the nested types within this
   * message are searched, then within the parent, on up to the root
   * namespace).
   * </pre>
   *
   * <code>optional string type_name = 6;</code>
   * @return The typeName.
   */
  java.lang.String getTypeName();
  /**
   * <pre>
   * For message and enum types, this is the name of the type.  If the name
   * starts with a '.', it is fully-qualified.  Otherwise, C++-like scoping
   * rules are used to find the type (i.e. first the nested types within this
   * message are searched, then within the parent, on up to the root
   * namespace).
   * </pre>
   *
   * <code>optional string type_name = 6;</code>
   * @return The bytes for typeName.
   */
  com.google.protobuf.ByteString
      getTypeNameBytes();

  /**
   * <pre>
   * description of this field
   * </pre>
   *
   * <code>optional string description = 7;</code>
   * @return Whether the description field is set.
   */
  boolean hasDescription();
  /**
   * <pre>
   * description of this field
   * </pre>
   *
   * <code>optional string description = 7;</code>
   * @return The description.
   */
  java.lang.String getDescription();
  /**
   * <pre>
   * description of this field
   * </pre>
   *
   * <code>optional string description = 7;</code>
   * @return The bytes for description.
   */
  com.google.protobuf.ByteString
      getDescriptionBytes();
}
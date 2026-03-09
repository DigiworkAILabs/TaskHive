// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'employee_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$EmployeeModelImpl _$$EmployeeModelImplFromJson(Map<String, dynamic> json) =>
    _$EmployeeModelImpl(
      id: json['id'] as String,
      userId: json['userId'] as String?,
      firstName: json['firstName'] as String,
      lastName: json['lastName'] as String,
      email: json['email'] as String,
      phone: json['phone'] as String?,
      address: json['address'] as String?,
      department: json['department'] as String?,
      designation: json['designation'] as String?,
      joinDate: json['joinDate'] as String?,
      photoUrl: json['photoUrl'] as String?,
      status: $enumDecode(_$EmployeeStatusEnumMap, json['status']),
      createdAt: json['createdAt'] as String?,
      updatedAt: json['updatedAt'] as String?,
    );

Map<String, dynamic> _$$EmployeeModelImplToJson(_$EmployeeModelImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      if (instance.userId case final value?) 'userId': value,
      'firstName': instance.firstName,
      'lastName': instance.lastName,
      'email': instance.email,
      if (instance.phone case final value?) 'phone': value,
      if (instance.address case final value?) 'address': value,
      if (instance.department case final value?) 'department': value,
      if (instance.designation case final value?) 'designation': value,
      if (instance.joinDate case final value?) 'joinDate': value,
      if (instance.photoUrl case final value?) 'photoUrl': value,
      'status': _$EmployeeStatusEnumMap[instance.status]!,
      if (instance.createdAt case final value?) 'createdAt': value,
      if (instance.updatedAt case final value?) 'updatedAt': value,
    };

const _$EmployeeStatusEnumMap = {
  EmployeeStatus.pending: 'PENDING',
  EmployeeStatus.active: 'ACTIVE',
  EmployeeStatus.inactive: 'INACTIVE',
  EmployeeStatus.deleted: 'DELETED',
};

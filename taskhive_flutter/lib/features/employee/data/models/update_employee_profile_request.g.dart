// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'update_employee_profile_request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$UpdateEmployeeProfileRequestImpl _$$UpdateEmployeeProfileRequestImplFromJson(
        Map<String, dynamic> json) =>
    _$UpdateEmployeeProfileRequestImpl(
      phone: json['phone'] as String?,
      address: json['address'] as String?,
    );

Map<String, dynamic> _$$UpdateEmployeeProfileRequestImplToJson(
        _$UpdateEmployeeProfileRequestImpl instance) =>
    <String, dynamic>{
      if (instance.phone case final value?) 'phone': value,
      if (instance.address case final value?) 'address': value,
    };

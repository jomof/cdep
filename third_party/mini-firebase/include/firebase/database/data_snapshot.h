// Copyright 2016 Google Inc. All Rights Reserved.

#ifndef FIREBASE_DATABASE_CLIENT_CPP_SRC_INCLUDE_FIREBASE_DATABASE_DATA_SNAPSHOT_H_
#define FIREBASE_DATABASE_CLIENT_CPP_SRC_INCLUDE_FIREBASE_DATABASE_DATA_SNAPSHOT_H_

#include <stddef.h>
#include <string>
#include "firebase/internal/common.h"
#include "firebase/variant.h"

namespace firebase {
namespace database {
namespace internal {
class Callbacks;
class DataSnapshotInternal;
class DatabaseReferenceInternal;
class QueryInternal;
}  // namespace internal

class Database;
class DatabaseReference;

/// A DataSnapshot instance contains data from a Firebase Database location. Any
/// time you read Database data, you receive the data as a DataSnapshot. These
/// are efficiently-generated and cannot be changed. To modify data,
/// use DatabaseReference::SetValue() or DatabaseReference::RunTransaction().
class DataSnapshot {
 public:
  /// @brief Copy constructor. DataSnapshots are immutable, so they can be
  /// efficiently copied.
  ///
  /// @param[in] snapshot DataSnapshot to copy.
  DataSnapshot(const DataSnapshot& snapshot);

  /// @brief Copy assignment operator. DataSnapshots are immutable, so they can
  /// be efficiently copied.
  ///
  /// @param[in] snapshot DataSnapshot to copy.
  ///
  /// @returns Reference to the destination DataSnapshot.
  DataSnapshot& operator=(const DataSnapshot& snapshot);

#if defined(FIREBASE_USE_MOVE_OPERATORS) || defined(DOXYGEN)
  /// @brief Move constructor. DataSnapshots are immutable, so they can be
  /// efficiently moved.
  ///
  /// @param[in] snapshot DataSnapshot to move into this one.
  DataSnapshot(DataSnapshot&& snapshot);

  /// @brief Move assignment operator. DataSnapshots are immutable, so they can
  /// be efficiently moved.
  ///
  /// @param[in] snapshot DataSnapshot to move into this one.
  ///
  /// @returns Reference to this destination DataSnapshot.
  DataSnapshot& operator=(DataSnapshot&& snapshot);
#endif  // defined(FIREBASE_USE_MOVE_OPERATORS) || defined(DOXYGEN)

  /// Destructor.
  ~DataSnapshot();

  /// @brief Returns true if the data is non-empty.
  bool Exists() const;

  /// @brief Get a DataSnapshot for the location at the specified relative path.
  ///
  /// @param[in] path Path relative to this snapshot's location.
  /// It only needs to be valid during this call.
  ///
  /// @returns A DataSnapshot corresponding to specified child location.
  DataSnapshot Child(const char* path) const;

  /// @brief Get a DataSnapshot for the location at the specified relative path.
  ///
  /// @param[in] path Path relative to this snapshot's location.
  ///
  /// @returns A DataSnapshot corresponding to specified child location.
  DataSnapshot Child(const std::string& path) const;

  /// @brief Get all the immediate children of this location.
  ///
  /// @returns The immediate children of this snapshot.
  std::vector<DataSnapshot> GetChildren() const;

  /// @brief Get the number of children of this location.
  ///
  /// @returns The number of immediate children of this snapshot.
  size_t GetChildrenCount() const;

  /// @brief Does this DataSnapshot have any children at all?
  ///
  /// @returns True if the snapshot has any children, false otherwise.
  bool HasChildren() const;

  /// @brief Get the key name of the source location of this snapshot.
  ///
  /// @note The returned pointer is only guaranteed to be valid while the
  /// DataSnapshot is still in memory.
  ///
  /// @returns Key name of the source location of this snapshot.
  const char* GetKey() const;

  /// @brief Get the key name of the source location of this snapshot.
  ///
  /// @returns Key name of the source location of this snapshot.
  std::string GetKeyString() const;

  /// @brief Get the value of the data contained in this snapshot.
  ///
  /// @returns The value of the data contained in this location.
  Variant GetValue() const;

  /// @brief Get the priority of the data contained in this snapshot.
  ///
  /// @returns The value of this location's Priority relative to its siblings.
  Variant GetPriority() const;

  /// @brief Obtain a DatabaseReference to the source location for this
  /// snapshot.
  ///
  /// @returns A DatabaseReference corresponding to same location as
  /// this snapshot.
  DatabaseReference GetReference() const;

  /// @brief Does this DataSnapshot have data at a particular location?
  ///
  /// @param[in] path Path relative to this snapshot's location.
  /// The pointer only needs to be valid during this call.
  ///
  /// @returns True if the snapshot has data at the specified location, false if
  /// not.
  bool HasChild(const char* path) const;

  /// @brief Does this DataSnapshot have data at a particular location?
  ///
  /// @param[in] path Path relative to this snapshot's location.
  ///
  /// @returns True if the snapshot has data at the specified location, false if
  /// not.
  bool HasChild(const std::string& path) const;

  /// @brief Returns true if this snapshot is valid, false if it is not
  /// valid. An invalid snapshot could be returned by a transaction where an
  /// error has occured.
  ///
  /// @returns true if this snapshot is valid, false if this snapshot is
  /// invalid.
  bool IsValid() const;

 private:
  /// @cond FIREBASE_APP_INTERNAL
  friend class internal::DatabaseReferenceInternal;
  friend class internal::DataSnapshotInternal;
  friend class internal::Callbacks;
  friend class internal::QueryInternal;
  /// @endcond

  DataSnapshot(internal::DataSnapshotInternal* internal);

  internal::DataSnapshotInternal* internal_;
};

}  // namespace database
}  // namespace firebase

#endif  // FIREBASE_DATABASE_CLIENT_CPP_SRC_INCLUDE_FIREBASE_DATABASE_DATA_SNAPSHOT_H_
